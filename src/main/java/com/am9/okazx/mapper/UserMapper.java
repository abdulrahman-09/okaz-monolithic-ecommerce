package com.am9.okazx.mapper;

import com.am9.okazx.model.dto.UserRequest;
import com.am9.okazx.model.dto.UserResponse;
import com.am9.okazx.model.entity.User;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = { AddressMapper.class }
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userRole", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    User toEntity(UserRequest request);

    UserResponse toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",       ignore = true)
    @Mapping(target = "userRole", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateEntityFromRequest(UserRequest request, @MappingTarget User user);
}
