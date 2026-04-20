package com.am9.okazx.service;

import com.am9.okazx.dto.request.CartItemRequest;
import com.am9.okazx.dto.response.CartItemResponse;

import java.util.List;

public interface CartService {

    void addToCart(Long userId, CartItemRequest cartItemRequest);
    void deleteFromCart(Long userId, Long productId);
    List<CartItemResponse> getCart(Long userId);
    void clearCart(Long userId);
}
