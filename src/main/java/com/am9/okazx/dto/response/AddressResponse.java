package com.am9.okazx.dto.response;

public record AddressResponse(
        String street,
        String city,
        String governorate,
        String country,
        String zipCode
) {}