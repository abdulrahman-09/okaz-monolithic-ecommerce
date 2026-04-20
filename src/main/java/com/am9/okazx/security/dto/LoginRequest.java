package com.am9.okazx.security.dto;

public record LoginRequest(
        String email,
        String password
) {
}
