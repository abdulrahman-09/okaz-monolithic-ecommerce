package com.am9.okazx.controller;

import com.am9.okazx.dto.request.UpdateOrderStatusRequest;
import com.am9.okazx.dto.response.OrderResponse;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order lifecycle management")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create order with app elements in current user cart")
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication
    ){
        Long userId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                orderService.createOrder(userId)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders")
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current user orders")
    public ResponseEntity<List<OrderResponse>> findMyOrders(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.findByUserId(user.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @orderServiceImpl.isOwner(#id, authentication.principal.id))")
    @Operation(summary = "Get order by order ID")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status by order id")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @orderServiceImpl.isOwner(#id, authentication.principal.id))")
    @Operation(summary = "Cancel order by order id")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
