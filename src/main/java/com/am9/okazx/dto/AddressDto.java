package com.am9.okazx.dto;

public record AddressDto(
        String street,
        String city,
        String governorate,
        String country,
        String zipCode
) {
}
