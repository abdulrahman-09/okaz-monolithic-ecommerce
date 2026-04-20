package com.am9.okazx.controller;

import com.am9.okazx.dto.request.CartItemRequest;
import com.am9.okazx.dto.response.CartItemResponse;
import com.am9.okazx.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartItemController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addToCart(
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody CartItemRequest cartItemRequest
            ){
        cartService.addToCart(userId, cartItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFromCart(
            @RequestHeader("X-User-ID") Long userId,
            @RequestParam Long productId
    ) {
        cartService.deleteFromCart(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> fetchCart(
            @RequestHeader("X-User-ID") Long userId){
            return ResponseEntity.ok(cartService.getCart(userId));
    }

}
