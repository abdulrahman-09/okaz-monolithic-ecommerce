package com.am9.okazx.service;

import com.am9.okazx.model.dto.OrderResponse;
import jakarta.transaction.Transactional;

public interface OrderService {
    OrderResponse createOrder(Long userId);
}
