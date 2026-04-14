package com.am9.okazx.model.dto;

import com.am9.okazx.model.entity.Address;
import com.am9.okazx.model.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        UserRole userRole,
        Address address,
        LocalDateTime creationTime
) {
}
