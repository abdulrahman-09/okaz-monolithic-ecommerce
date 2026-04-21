package com.am9.okazx.service.impl;

import com.am9.okazx.exception.UserAlreadyExistsException;
import com.am9.okazx.mapper.AddressMapper;
import com.am9.okazx.security.dto.AuthenticationResponse;
import com.am9.okazx.security.dto.LoginRequest;
import com.am9.okazx.security.dto.RegisterRequest;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.model.enums.UserRole;
import com.am9.okazx.repository.UserRepository;
import com.am9.okazx.security.service.JwtService;
import com.am9.okazx.service.AuthenticationService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AddressMapper addressMapper;


    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        User user = User
                .builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .address(addressMapper.toEntity(request.addressDto()))
                .userRole(UserRole.CUSTOMER)
                .build();

        String token = jwtService.generateToken(
                userRepository.save(user)
        );

        return new  AuthenticationResponse(token);
    }

    @Override
    public AuthenticationResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }

    @Override
    @Transactional
    public AuthenticationResponse registerAdminUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        User adminUser = User
                .builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .address(addressMapper.toEntity(request.addressDto()))
                .userRole(UserRole.ADMIN)
                .build();

        String token = jwtService.generateToken(
                userRepository.save(adminUser)
        );
        return new  AuthenticationResponse(token);
    }
}
