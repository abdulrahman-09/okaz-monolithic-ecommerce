package com.am9.okazx.controller;

import com.am9.okazx.dto.request.CartItemRequest;
import com.am9.okazx.dto.response.CartItemResponse;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management — CUSTOMER only")
public class CartItemController {
    private final CartService cartService;

    @PostMapping
    @Operation(summary = "Add item to cart")
    public ResponseEntity<Void> addToCart(
            Authentication authentication,
            @RequestBody @Valid CartItemRequest cartItemRequest
            ){
        Long userId =((User) authentication.getPrincipal()).getId();
        cartService.addToCart(userId, cartItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<Void> deleteFromCart(
            @RequestParam Long productId,
            Authentication authentication
    ) {
        Long userId =((User) authentication.getPrincipal()).getId();
        cartService.deleteFromCart(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<List<CartItemResponse>> fetchCart(Authentication authentication){
        Long userId =((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(cartService.getCart(userId));
    }

}
