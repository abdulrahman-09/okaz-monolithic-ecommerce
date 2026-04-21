package com.am9.okazx.mapper;

import com.am9.okazx.dto.request.AddressRequest;
import com.am9.okazx.dto.response.AddressResponse;
import com.am9.okazx.model.entity.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toEntity(AddressRequest request);

    AddressResponse toDto(Address address);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(AddressRequest request, @MappingTarget Address address);

}
