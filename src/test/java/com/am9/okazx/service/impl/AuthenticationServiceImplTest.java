package com.am9.okazx.service.impl;

import com.am9.okazx.dto.request.AddressRequest;
import com.am9.okazx.exception.UserAlreadyExistsException;
import com.am9.okazx.mapper.AddressMapper;
import com.am9.okazx.model.entity.Address;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.model.enums.UserRole;
import com.am9.okazx.repository.UserRepository;
import com.am9.okazx.security.dto.AuthenticationResponse;
import com.am9.okazx.security.dto.LoginRequest;
import com.am9.okazx.security.dto.RegisterRequest;
import com.am9.okazx.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationServiceImpl Unit Tests")
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private Address testAddress;
    private AddressRequest addressRequest;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "TestPassword123";
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final String TEST_TOKEN = "test_jwt_token";

    @BeforeEach
    void setUp() {
        testAddress = Address.builder()
                .street("123 Test Street")
                .city("Test City")
                .governorate("Tg")
                .zipCode("12345")
                .country("Test Country")
                .build();

        addressRequest = new AddressRequest(
                "123 Test Street",
                "Test City",
                "TC",
                "12345",
                "Test Country"
        );

        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .phone("+1234567890")
                .address(testAddress)
                .userRole(UserRole.CUSTOMER)
                .build();

        registerRequest = new RegisterRequest(
                "John",
                "Doe",
                TEST_EMAIL,
                "+1234567890",
                addressRequest,
                TEST_PASSWORD
        );

        loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
    }

    @Test
    @DisplayName("register should create a new customer user successfully with all validations")
    void register_WithValidRequest_ShouldCreateCustomerUserWithAllValidations() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(addressMapper.toEntity(addressRequest)).thenReturn(testAddress);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Validate role
            assertEquals(UserRole.CUSTOMER, savedUser.getUserRole());
            // Validate password encoding
            assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
            // Validate other details
            assertEquals("John", savedUser.getFirstName());
            assertEquals("Doe", savedUser.getLastName());
            assertEquals(TEST_EMAIL, savedUser.getEmail());
            assertEquals("+1234567890", savedUser.getPhone());
            assertEquals(testAddress, savedUser.getAddress());
            return savedUser;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn(TEST_TOKEN);

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.token());
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(addressMapper).toEntity(addressRequest);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("register should throw UserAlreadyExistsException when email is already registered")
    void register_WithExistingEmail_ShouldThrowUserAlreadyExistsException() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> authenticationService.register(registerRequest),
                "Should throw UserAlreadyExistsException for existing email");

        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("authenticate should return authentication response with token on valid credentials")
    void authenticate_WithValidCredentials_ShouldReturnAuthenticationResponse() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(TEST_EMAIL, TEST_PASSWORD);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authToken);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(TEST_TOKEN);

        // Act
        AuthenticationResponse response = authenticationService.authenticate(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    @DisplayName("authenticate should extract user principal from authentication object")
    void authenticate_WithValidCredentials_ShouldExtractUserPrincipal() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(testUser, TEST_PASSWORD);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authToken);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(TEST_TOKEN);

        // Act
        authenticationService.authenticate(loginRequest);

        // Assert
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    @DisplayName("registerAdminUser should create a new admin user successfully with all validations")
    void registerAdminUser_WithValidRequest_ShouldCreateAdminUserWithAllValidations() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .firstName("Admin")
                .lastName("User")
                .email("admin@example.com")
                .password(ENCODED_PASSWORD)
                .phone("+1234567890")
                .address(testAddress)
                .userRole(UserRole.ADMIN)
                .build();

        RegisterRequest adminRegisterRequest = new RegisterRequest(
                "Admin",
                "User",
                "admin@example.com",
                "+1234567890",
                addressRequest,
                TEST_PASSWORD
        );

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(addressMapper.toEntity(addressRequest)).thenReturn(testAddress);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Validate role
            assertEquals(UserRole.ADMIN, savedUser.getUserRole());
            // Validate password encoding
            assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
            // Validate other details
            assertEquals("Admin", savedUser.getFirstName());
            assertEquals("User", savedUser.getLastName());
            assertEquals("admin@example.com", savedUser.getEmail());
            assertEquals("+1234567890", savedUser.getPhone());
            assertEquals(testAddress, savedUser.getAddress());
            return savedUser;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn(TEST_TOKEN);

        // Act
        AuthenticationResponse response = authenticationService.registerAdminUser(adminRegisterRequest);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.token());
        verify(userRepository).findByEmail("admin@example.com");
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(addressMapper).toEntity(addressRequest);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("registerAdminUser should throw UserAlreadyExistsException when email is already registered")
    void registerAdminUser_WithExistingEmail_ShouldThrowUserAlreadyExistsException() {
        // Arrange
        RegisterRequest adminRegisterRequest = new RegisterRequest(
                "Admin",
                "User",
                TEST_EMAIL,
                "+1234567890",
                addressRequest,
                TEST_PASSWORD
        );

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> authenticationService.registerAdminUser(adminRegisterRequest),
                "Should throw UserAlreadyExistsException for existing email");

        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

}
