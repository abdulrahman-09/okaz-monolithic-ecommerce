package com.am9.okazx.security.dto;

import com.am9.okazx.model.entity.Address;

public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        Address address,
        String password
) {
}
