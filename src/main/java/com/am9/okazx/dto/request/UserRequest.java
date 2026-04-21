package com.am9.okazx.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record UserRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number is invalid")
        String phone,

        @NotNull(message = "Address is required")
        @Valid
        AddressRequest addressRequest
) {}
