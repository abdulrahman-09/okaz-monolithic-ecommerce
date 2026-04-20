package com.am9.okazx.dto.response;


import com.am9.okazx.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        BigDecimal totalPrice,
        OrderStatus status,
        List<OrderItemResponse> items,
        LocalDateTime creationTime

) {
}
