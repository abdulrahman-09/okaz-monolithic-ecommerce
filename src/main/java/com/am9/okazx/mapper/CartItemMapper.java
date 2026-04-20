package com.am9.okazx.mapper;

import com.am9.okazx.dto.response.CartItemResponse;
import com.am9.okazx.model.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = { ProductMapper.class }
)
public interface CartItemMapper {

    @Mapping(target = "userId", source = "user.id")  // add this
    CartItemResponse toDto(CartItem cartItem);

}
