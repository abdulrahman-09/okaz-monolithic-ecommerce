package com.am9.okazx.service.impl;

import com.am9.okazx.dto.request.AddressRequest;
import com.am9.okazx.dto.request.UserRequest;
import com.am9.okazx.dto.response.AddressResponse;
import com.am9.okazx.dto.response.PageResponse;
import com.am9.okazx.dto.response.UserResponse;
import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.exception.UserAlreadyExistsException;
import com.am9.okazx.mapper.UserMapper;
import com.am9.okazx.model.entity.Address;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.model.enums.UserRole;
import com.am9.okazx.repository.OrderRepository;
import com.am9.okazx.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRequest userRequest;
    private UserResponse userResponse;
    private Address testAddress;
    private AddressRequest addressRequest;
    private AddressResponse addressResponse;
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String UPDATED_EMAIL = "updated@example.com";

    @BeforeEach
    void setUp() {
        testAddress = Address.builder()
                .street("123 Test Street")
                .city("Test City")
                .governorate("TG")
                .zipCode("12345")
                .country("Test Country")
                .build();

        addressRequest = new AddressRequest(
                "123 Test Street",
                "Test City",
                "TG",
                "Test Country",
                "12345"
        );

        addressResponse = new AddressResponse(
                "123 Test Street",
                "Test City",
                "TG",
                "Test Country",
                "12345"
        );

        testUser = User.builder()
                .id(TEST_USER_ID)
                .firstName("John")
                .lastName("Doe")
                .email(TEST_EMAIL)
                .password("encoded_password")
                .phone("+1234567890")
                .address(testAddress)
                .userRole(UserRole.CUSTOMER)
                .creationTime(LocalDateTime.now())
                .build();

        userRequest = new UserRequest(
                "Jane",
                "Smith",
                UPDATED_EMAIL,
                "+0987654321",
                addressRequest
        );

        userResponse = new UserResponse(
                TEST_USER_ID,
                "Jane",
                "Smith",
                UPDATED_EMAIL,
                "+0987654321",
                UserRole.CUSTOMER,
                addressResponse,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("findAll should return PageResponse with paginated users")
    void findAll_ShouldReturnPageResponseWithPaginatedUsers() {
        // Given
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id,asc";

        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        // When
        PageResponse<UserResponse> result = userService.findAll(pageNo, pageSize, sortBy);

        // Then
        assertEquals(1, result.content().size());
        assertEquals(userResponse, result.content().get(0));
        assertEquals(0, result.pageNo());
        assertEquals(10, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.first());
        assertTrue(result.last());

        verify(userRepository).findAll(any(Pageable.class));
        verify(userMapper).toDto(testUser);
    }

    @Test
    @DisplayName("findAll should throw IllegalArgumentException for invalid sort direction")
    void findAll_InvalidSortDirection_ShouldThrowIllegalArgumentException() {
        // Given
        String sortBy = "email,invalid";  // Invalid direction

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.findAll(0, 10, sortBy));

        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("findAll should sort by different fields correctly")
    void findAll_DifferentSortFields_ShouldApplyCorrectSorting() {
        // Given
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "email,desc";

        Sort sort = Sort.by(Sort.Direction.DESC, "email");
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 50);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        // When
        PageResponse<UserResponse> result = userService.findAll(pageNo, pageSize, sortBy);

        // Then
        assertEquals(50, result.totalElements());
        assertFalse(result.last());

        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("findById should return user response when user exists")
    void findById_ShouldReturnUserResponse_WhenUserExists() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.findById(TEST_USER_ID);

        // Assert
        assertEquals(userResponse, result);
        verify(userRepository).findById(TEST_USER_ID);
        verify(userMapper).toDto(testUser);
    }

    @Test
    @DisplayName("findById should throw ResourceNotFoundException when user does not exist")
    void findById_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.findById(TEST_USER_ID),
                "User not found with id: " + TEST_USER_ID);
        verify(userRepository).findById(TEST_USER_ID);
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    @DisplayName("update should return updated user response when valid")
    void update_ShouldReturnUpdatedUserResponse_WhenValid() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(UPDATED_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        // Act
        UserResponse result = userService.update(TEST_USER_ID, userRequest);

        // Assert
        assertEquals(userResponse, result);
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).findByEmail(UPDATED_EMAIL);
        verify(userMapper).updateEntityFromRequest(userRequest, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).toDto(testUser);
    }

    @Test
    @DisplayName("update should throw ResourceNotFoundException when user does not exist")
    void update_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.update(TEST_USER_ID, userRequest),
                "User not found with id: " + TEST_USER_ID);
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userMapper, never()).updateEntityFromRequest(any(), any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("update should throw UserAlreadyExistsException when email is already in use")
    void update_ShouldThrowUserAlreadyExistsException_WhenEmailAlreadyInUse() {
        // Arrange
        User anotherUser = User.builder().id(2L).email(UPDATED_EMAIL).build();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(UPDATED_EMAIL)).thenReturn(Optional.of(anotherUser));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.update(TEST_USER_ID, userRequest),
                "Email already in use");
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).findByEmail(UPDATED_EMAIL);
        verify(userMapper, never()).updateEntityFromRequest(any(), any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("delete should delete user when exists")
    void delete_ShouldDeleteUser_WhenExists() {
        // Arrange
        when(userRepository.existsById(TEST_USER_ID)).thenReturn(true);

        // Act
        userService.delete(TEST_USER_ID);

        // Assert
        verify(userRepository).existsById(TEST_USER_ID);
        verify(orderRepository).detachFromUser(TEST_USER_ID);
        verify(userRepository).deleteById(TEST_USER_ID);
    }

    @Test
    @DisplayName("delete should throw ResourceNotFoundException when user does not exist")
    void delete_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.existsById(TEST_USER_ID)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.delete(TEST_USER_ID),
                "User not found with id: " + TEST_USER_ID);
        verify(userRepository).existsById(TEST_USER_ID);
        verify(orderRepository, never()).detachFromUser(anyLong());
        verify(userRepository, never()).deleteById(anyLong());
    }
}