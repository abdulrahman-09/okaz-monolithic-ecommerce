package com.am9.okazx.mapper;

import com.am9.okazx.dto.response.OrderItemResponse;

import com.am9.okazx.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = { java.math.BigDecimal.class })
public interface OrderItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productPrice", source = "product.price")
    @Mapping(target = "totalPrice", expression = "java(orderItem.getProduct().getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))")
    OrderItemResponse toDto(OrderItem orderItem);
}
