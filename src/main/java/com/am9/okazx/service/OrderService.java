package com.am9.okazx.service;

import com.am9.okazx.dto.UpdateOrderStatusRequest;
import com.am9.okazx.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(Long userId);
    List<OrderResponse> findAll();
    List<OrderResponse> findByUserId(Long id);
    OrderResponse findById(Long id);
    OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request);
    OrderResponse cancelOrder(Long id);
    boolean isOwner(Long orderId, Long userId);
}

