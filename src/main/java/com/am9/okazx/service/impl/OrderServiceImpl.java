package com.am9.okazx.service.impl;

import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.mapper.OrderMapper;
import com.am9.okazx.dto.response.OrderResponse;
import com.am9.okazx.model.entity.CartItem;
import com.am9.okazx.model.entity.Order;
import com.am9.okazx.model.entity.OrderItem;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.model.enums.OrderStatus;
import com.am9.okazx.repository.CartItemRepository;
import com.am9.okazx.repository.OrderRepository;
import com.am9.okazx.repository.UserRepository;
import com.am9.okazx.service.CartService;
import com.am9.okazx.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;

    @Transactional
    @Override
    public OrderResponse createOrder(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new ResourceNotFoundException("Cart is empty for user with id: " + userId);
        }

        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.CONFIRMED);

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> new OrderItem(null, item.getProduct(), item.getQuantity(), item.getPrice(), order))
                .toList();

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        cartService.clearCart(userId);

        return orderMapper.toDto(savedOrder);
    }
}
