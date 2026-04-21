package com.am9.okazx.service.impl;

import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.mapper.CartItemMapper;
import com.am9.okazx.dto.request.CartItemRequest;
import com.am9.okazx.dto.response.CartItemResponse;
import com.am9.okazx.model.entity.CartItem;
import com.am9.okazx.model.entity.Product;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.repository.CartItemRepository;
import com.am9.okazx.repository.ProductRepository;
import com.am9.okazx.repository.UserRepository;
import com.am9.okazx.service.CartService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartIServiceImpl implements CartService {
    private final CartItemMapper cartItemMapper;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public void addToCart(Long userId, CartItemRequest cartItemRequest) {
        Product product = productRepository.findById(cartItemRequest.productId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product not found with id: " +
                                cartItemRequest.productId())
                );
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        cartItemRepository.findByUserAndProduct(user, product)
                .ifPresentOrElse(
                        existing -> updateCartItem(existing, product, cartItemRequest.quantity()),
                        () -> createCartItem(user, product, cartItemRequest.quantity())
                );

    }

    @Override
    @Transactional
    public void deleteFromCart(Long userId, Long productId) {
        if (!cartItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Cart item not found");
        }
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return cartItemRepository.findByUser(user)
                .stream()
                .map(cartItemMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        userRepository.findById(userId).ifPresent(
                cartItemRepository::deleteByUser
        );
    }


    // Helper methods
    private void createCartItem(User user, Product product, Integer quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        cartItemRepository.save(cartItem);
    }

    private void updateCartItem(CartItem cartItem, Product product, Integer quantity) {
        int newQuantity = cartItem.getQuantity() + quantity;
        cartItem.setQuantity(newQuantity);
        cartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
        cartItemRepository.save(cartItem);
    }
}
