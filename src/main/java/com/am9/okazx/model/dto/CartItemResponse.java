package com.am9.okazx.model.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long userId,
        ProductResponse product,
        Integer quantity,
        BigDecimal price
) {}
