package com.am9.okazx.service.impl;

import com.am9.okazx.dto.request.UpdateOrderStatusRequest;
import com.am9.okazx.exception.InsufficientStockException;
import com.am9.okazx.exception.InvalidOrderStatusTransitionException;
import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.mapper.OrderMapper;
import com.am9.okazx.dto.response.OrderResponse;
import com.am9.okazx.model.entity.*;
import com.am9.okazx.model.enums.OrderStatus;
import com.am9.okazx.repository.CartItemRepository;
import com.am9.okazx.repository.OrderRepository;
import com.am9.okazx.repository.UserRepository;
import com.am9.okazx.service.CartService;
import com.am9.okazx.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

        //validate and decrement stock before persisting the order
        cartItems.forEach(item -> {
            Product product = item.getProduct();
            int requested = item.getQuantity();
            if (product.getStockQuantity() == null || product.getStockQuantity() < requested) {
                throw new InsufficientStockException(
                        "Not enough stock for product: '" + product.getName()
                                + "'. Available: " + (product.getStockQuantity() == null ? 0 : product.getStockQuantity())
                                + ", requested: " + requested
                );
            }
            product.setStockQuantity(product.getStockQuantity() - requested);
        });

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> new OrderItem(null, item.getProduct(), item.getQuantity(), item.getPrice(), order))
                .toList();

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        cartService.clearCart(userId);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByUserId(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user with id: " + id));

        return orderRepository.findAllByUser(user)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No order with id: " + id));
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        validateStatusTransition(order.getStatus(), request.status());

        order.setStatus(request.status());
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        validateStatusTransition(order.getStatus(), OrderStatus.CANCELED);

        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        });

        order.setStatus(OrderStatus.CANCELED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUser().getId().equals(userId))
                .orElse(false);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        Map<OrderStatus, List<OrderStatus>> allowed = Map.of(
                OrderStatus.CONFIRMED, List.of(OrderStatus.SHIPPED,    OrderStatus.CANCELED),
                OrderStatus.SHIPPED,   List.of(OrderStatus.DELIVERED),
                OrderStatus.DELIVERED, List.of(),
                OrderStatus.CANCELED,  List.of()
        );

        if (!allowed.get(current).contains(next)) {
            throw new InvalidOrderStatusTransitionException(
                    "Cannot transition from " + current + " to " + next
            );
        }
    }
}
