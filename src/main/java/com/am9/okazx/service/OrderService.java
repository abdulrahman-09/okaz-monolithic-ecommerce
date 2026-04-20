package com.am9.okazx.service;

import com.am9.okazx.dto.response.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(Long userId);
}
