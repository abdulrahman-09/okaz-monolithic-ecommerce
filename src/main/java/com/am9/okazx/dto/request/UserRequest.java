package com.am9.okazx.dto.request;

import com.am9.okazx.model.entity.Address;

public record UserRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        Address address

) { }
