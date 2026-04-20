package com.am9.okazx.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    Long productId,
    Integer quantity,
    BigDecimal productPrice,
    BigDecimal totalPrice

) {
}
