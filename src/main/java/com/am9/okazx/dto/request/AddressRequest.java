package com.am9.okazx.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressRequest(
        @NotBlank(message = "Street is required")
        String street,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Governorate is required")
        String governorate,

        @NotBlank(message = "Country is required")
        String country,

        @Pattern(regexp = "^[0-9]{5}$", message = "Zip code must be 5 digits")
        String zipCode
) {}