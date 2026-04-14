package com.am9.okazx.model.dto;

import java.math.BigDecimal;

public record ProductResponse(

         Long id,
         String name,
         String description,
         BigDecimal price,
         Integer stockQuantity,
         String category,
         String imageUrl

) {
}
