package com.am9.okazx.service;

import com.am9.okazx.model.dto.CartItemRequest;
import com.am9.okazx.model.dto.CartItemResponse;

import java.util.List;

public interface CartService {

    void addToCart(Long userId, CartItemRequest cartItemRequest);
    void deleteFromCart(Long userId, Long productId);
    List<CartItemResponse> getCart(Long userId);
    void clearCart(Long userId);
}
