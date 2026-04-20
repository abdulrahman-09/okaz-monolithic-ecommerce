package com.am9.okazx.service;

import com.am9.okazx.security.dto.AuthenticationResponse;
import com.am9.okazx.security.dto.LoginRequest;
import com.am9.okazx.security.dto.RegisterRequest;

public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest request);
    AuthenticationResponse authenticate(LoginRequest request);

}
