package com.am9.okazx.model.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    Long productId,
    Integer quantity,
    BigDecimal productPrice,
    BigDecimal totalPrice

) {
}
