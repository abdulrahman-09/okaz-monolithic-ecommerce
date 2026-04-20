package com.am9.okazx.controller;

import com.am9.okazx.dto.response.OrderResponse;
import com.am9.okazx.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> creatOrder(
            @RequestHeader("X-User-ID") Long userId){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                orderService.createOrder(userId)
        );
    }
}
