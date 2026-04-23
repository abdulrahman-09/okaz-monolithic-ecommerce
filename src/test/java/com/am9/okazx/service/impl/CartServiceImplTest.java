package com.am9.okazx.service.impl;

import com.am9.okazx.dto.request.CartItemRequest;
import com.am9.okazx.dto.response.CartItemResponse;
import com.am9.okazx.dto.response.ProductResponse;
import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.mapper.CartItemMapper;
import com.am9.okazx.model.entity.CartItem;
import com.am9.okazx.model.entity.Product;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.model.enums.UserRole;
import com.am9.okazx.repository.CartItemRepository;
import com.am9.okazx.repository.ProductRepository;
import com.am9.okazx.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart service test")
public class CartServiceImplTest {

    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @InjectMocks
    private CartServiceImpl cartService;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 1L;
    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;
    private CartItemResponse testCartItemResponse;
    private CartItemRequest testCartItemRequest;
    private ProductResponse testProductResponse;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(19.99))
                .stockQuantity(100)
                .category("Test Category")
                .imageUrl("http://example.com/image.jpg")
                .active(true)
                .build();

        testProductResponse = new ProductResponse(
                PRODUCT_ID,
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(19.99),
                100,
                "Test Category",
                "http://example.com/image.jpg"
        );

        testUser = User.builder()
                .id(USER_ID)
                .firstName("am9")
                .lastName("Mujahid")
                .email("am9@mujahid.com")
                .phone("1234567890")
                .userRole(UserRole.CUSTOMER)
                .build();

        testCartItem = CartItem.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .quantity(2)
                .price(BigDecimal.valueOf(39.98))
                .build();

        testCartItemResponse = new CartItemResponse(
                1L, USER_ID, testProductResponse, 2, BigDecimal.valueOf(39.98)
        );

        testCartItemRequest = new CartItemRequest(PRODUCT_ID, 3);
    }

    @Test
    @DisplayName("Get user's cart")
    void testGetCart_shouldReturnListOfCartItemResponse() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUser(testUser)).thenReturn(List.of(testCartItem));
        when(cartItemMapper.toDto(testCartItem)).thenReturn(testCartItemResponse);

        // Act
        List<CartItemResponse> result = cartService.getCart(USER_ID);

        // Assert
        assertEquals(List.of(testCartItemResponse), result);
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository).findByUser(testUser);
        verify(cartItemMapper).toDto(testCartItem);
    }

    @Test
    @DisplayName("Get cart should throw ResourceNotFoundException when user not found")
    void testGetCart_shouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> cartService.getCart(USER_ID),
                "User not found with id: " + USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository, never()).findByUser(any(User.class));
    }

    @Test
    @DisplayName("Add to cart should create new cart item when not exists")
    void testAddToCart_shouldCreateNewCartItem_WhenNotExists() {
        // Arrange
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserAndProduct(testUser, testProduct)).thenReturn(Optional.empty());

        // Act
        cartService.addToCart(USER_ID, testCartItemRequest);

        // Assert
        verify(productRepository).findById(PRODUCT_ID);
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository).findByUserAndProduct(testUser, testProduct);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Add to cart should update existing cart item when exists")
    void testAddToCart_shouldUpdateExistingCartItem_WhenExists() {
        // Arrange
        CartItem existingCartItem = CartItem.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .quantity(2)
                .price(BigDecimal.valueOf(39.98))
                .build();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserAndProduct(testUser, testProduct)).thenReturn(Optional.of(existingCartItem));

        // Act
        cartService.addToCart(USER_ID, testCartItemRequest);

        // Assert
        verify(productRepository).findById(PRODUCT_ID);
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository).findByUserAndProduct(testUser, testProduct);
        verify(cartItemRepository).save(existingCartItem);
        assertEquals(5, existingCartItem.getQuantity()); // 2 + 3
        assertEquals(BigDecimal.valueOf(99.95), existingCartItem.getPrice()); // 19.99 * 5
    }

    @Test
    @DisplayName("Add to cart should throw ResourceNotFoundException when product not found")
    void testAddToCart_shouldThrowResourceNotFoundException_WhenProductNotFound() {
        // Arrange
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> cartService.addToCart(USER_ID, testCartItemRequest),
                "Product not found with id: " + PRODUCT_ID);
        verify(productRepository).findById(PRODUCT_ID);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Add to cart should throw ResourceNotFoundException when user not found")
    void testAddToCart_shouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Arrange
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> cartService.addToCart(USER_ID, testCartItemRequest),
                "User not found with id: " + USER_ID);
        verify(productRepository).findById(PRODUCT_ID);
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository, never()).findByUserAndProduct(any(), any());
    }

    @Test
    @DisplayName("Delete from cart should delete cart item when exists")
    void testDeleteFromCart_shouldDeleteCartItem_WhenExists() {
        // Arrange
        when(cartItemRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(true);

        // Act
        cartService.deleteFromCart(USER_ID, PRODUCT_ID);

        // Assert
        verify(cartItemRepository).existsByUserIdAndProductId(USER_ID, PRODUCT_ID);
        verify(cartItemRepository).deleteByUserIdAndProductId(USER_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("Delete from cart should throw ResourceNotFoundException when cart item not found")
    void testDeleteFromCart_shouldThrowResourceNotFoundException_WhenNotFound() {
        // Arrange
        when(cartItemRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> cartService.deleteFromCart(USER_ID, PRODUCT_ID),
                "Cart item not found");
        verify(cartItemRepository).existsByUserIdAndProductId(USER_ID, PRODUCT_ID);
        verify(cartItemRepository, never()).deleteByUserIdAndProductId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Clear cart should delete all cart items for user")
    void testClearCart_shouldDeleteAllCartItemsForUser() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        cartService.clearCart(USER_ID);

        // Assert
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository).deleteByUser(testUser);
    }

    @Test
    @DisplayName("Clear cart should do nothing when user not found")
    void testClearCart_shouldDoNothing_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act
        cartService.clearCart(USER_ID);

        // Assert
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository, never()).deleteByUser(any(User.class));
    }
}
