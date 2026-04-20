package com.am9.okazx.controller;

import com.am9.okazx.dto.request.CartItemRequest;
import com.am9.okazx.dto.response.CartItemResponse;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartItemController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addToCart(
            Authentication authentication,
            @RequestBody CartItemRequest cartItemRequest
            ){
        Long userId =((User) authentication.getPrincipal()).getId();
        cartService.addToCart(userId, cartItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFromCart(
            @RequestParam Long productId,
            Authentication authentication
    ) {
        Long userId =((User) authentication.getPrincipal()).getId();
        cartService.deleteFromCart(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> fetchCart(Authentication authentication){
        Long userId =((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(cartService.getCart(userId));
    }

}
