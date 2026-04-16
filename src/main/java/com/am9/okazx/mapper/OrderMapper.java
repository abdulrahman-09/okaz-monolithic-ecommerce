package com.am9.okazx.mapper;

import com.am9.okazx.model.dto.OrderResponse;
import com.am9.okazx.model.entity.Order;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = { OrderItemMapper.class }
)
public interface OrderMapper {

    OrderResponse toDto(Order order);

}
