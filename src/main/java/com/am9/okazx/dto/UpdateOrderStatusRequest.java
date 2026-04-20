package com.am9.okazx.dto;

import com.am9.okazx.model.enums.OrderStatus;

public record UpdateOrderStatusRequest(
        OrderStatus status
) {
}
