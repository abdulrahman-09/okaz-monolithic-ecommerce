package com.am9.okazx.service;

import com.am9.okazx.dto.request.UpdateOrderStatusRequest;
import com.am9.okazx.dto.response.OrderResponse;
import com.am9.okazx.dto.response.PageResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(Long userId);
    PageResponse<OrderResponse> findAll(int pageNo, int pageSize, String sortBy);
    List<OrderResponse> findByUserId(Long id);
    OrderResponse findById(Long id);
    OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request);
    OrderResponse cancelOrder(Long id);
    boolean isOwner(Long orderId, Long userId);
}

