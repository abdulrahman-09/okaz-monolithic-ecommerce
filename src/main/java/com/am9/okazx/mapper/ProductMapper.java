package com.am9.okazx.mapper;

import com.am9.okazx.model.dto.ProductRequest;
import com.am9.okazx.model.dto.ProductResponse;
import com.am9.okazx.model.dto.UserRequest;
import com.am9.okazx.model.entity.Product;
import com.am9.okazx.model.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    Product toEntity(ProductRequest productRequest);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ProductRequest productRequest, @MappingTarget Product product);

}
