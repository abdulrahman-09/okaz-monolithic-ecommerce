package com.am9.okazx.controller;


import com.am9.okazx.security.dto.AuthenticationResponse;
import com.am9.okazx.security.dto.LoginRequest;
import com.am9.okazx.security.dto.RegisterRequest;
import com.am9.okazx.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @PostMapping("/register")
    @Operation(summary = "Register a customer user")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request));
    }

    @PostMapping("/admin/register")             // resolves to /api/v1/admin/users
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a admin user only by admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.registerAdminUser(request));
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid LoginRequest request){
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

}
