package com.am9.okazx.model.dto;

public record AddressDto(
        String street,
        String city,
        String governorate,
        String country,
        String zipCode
) {
}
