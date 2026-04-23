package com.am9.okazx.service.impl;

import com.am9.okazx.dto.request.UpdateOrderStatusRequest;
import com.am9.okazx.dto.response.OrderItemResponse;
import com.am9.okazx.dto.response.OrderResponse;
import com.am9.okazx.exception.InsufficientStockException;
import com.am9.okazx.exception.InvalidOrderStatusTransitionException;
import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.mapper.OrderMapper;
import com.am9.okazx.model.entity.*;
import com.am9.okazx.model.enums.OrderStatus;
import com.am9.okazx.model.enums.UserRole;
import com.am9.okazx.repository.CartItemRepository;
import com.am9.okazx.repository.OrderRepository;
import com.am9.okazx.repository.UserRepository;
import com.am9.okazx.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CartService cartService;
    @Mock
    private OrderMapper orderMapper;
    @InjectMocks
    private OrderServiceImpl orderService;

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 1L;
    private static final Long PRODUCT_ID = 1L;
    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;
    private Order testOrder;
    private OrderResponse testOrderResponse;
    private UpdateOrderStatusRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(USER_ID)
                .firstName("am9")
                .lastName("mujahid")
                .email("am9@mujahid.com")
                .phone("1234567890")
                .userRole(UserRole.CUSTOMER)
                .build();

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

        testCartItem = CartItem.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .quantity(2)
                .price(BigDecimal.valueOf(39.98))
                .build();

        OrderItem orderItem = new OrderItem(1L, testProduct, 2, BigDecimal.valueOf(19.99), null);

        testOrder = new Order();
        testOrder.setId(ORDER_ID);
        testOrder.setUser(testUser);
        testOrder.setTotalPrice(BigDecimal.valueOf(39.98));
        testOrder.setStatus(OrderStatus.CONFIRMED);
        testOrder.setItems(List.of(orderItem));
        testOrder.setCreationTime(LocalDateTime.now());

        OrderItemResponse orderItemResponse = new OrderItemResponse(
                1L, PRODUCT_ID, 2, BigDecimal.valueOf(19.99), BigDecimal.valueOf(39.98)
        );

        testOrderResponse = new OrderResponse(
                ORDER_ID, BigDecimal.valueOf(39.98), OrderStatus.CONFIRMED,
                List.of(orderItemResponse), LocalDateTime.now()
        );

        updateRequest = new UpdateOrderStatusRequest(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("createOrder should create order successfully when cart has items and sufficient stock")
    void createOrder_ShouldCreateOrder_WhenValid() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUser(testUser)).thenReturn(List.of(testCartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDto(testOrder)).thenReturn(testOrderResponse);

        // Act
        OrderResponse result = orderService.createOrder(USER_ID);

        // Assert
        assertEquals(testOrderResponse, result);
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository).findByUser(testUser);
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart(USER_ID);
        verify(orderMapper).toDto(testOrder);
        assertEquals(98, testProduct.getStockQuantity()); // 100 - 2
    }

    @Test
    @DisplayName("createOrder should throw ResourceNotFoundException when user not found")
    void createOrder_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(USER_ID),
                "User not found with id: " + USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository, never()).findByUser(any(User.class));
    }

    @Test
    @DisplayName("createOrder should throw ResourceNotFoundException when cart is empty")
    void createOrder_ShouldThrowResourceNotFoundException_WhenCartEmpty() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUser(testUser)).thenReturn(List.of());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(USER_ID),
                "Cart is empty for user with id: " + USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository).findByUser(testUser);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder should throw InsufficientStockException when stock is insufficient")
    void createOrder_ShouldThrowInsufficientStockException_WhenInsufficientStock() {
        // Arrange
        testProduct.setStockQuantity(1); // Less than requested 2
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUser(testUser)).thenReturn(List.of(testCartItem));

        // Act & Assert
        assertThrows(InsufficientStockException.class,
                () -> orderService.createOrder(USER_ID),
                "Not enough stock for product: 'Test Product'. Available: 1, requested: 2");
        verify(userRepository).findById(USER_ID);
        verify(cartItemRepository).findByUser(testUser);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("findAll should return list of order responses")
    void findAll_ShouldReturnListOfOrderResponses() {
        // Arrange
        List<Order> orders = List.of(testOrder);
        List<OrderResponse> expectedResponses = List.of(testOrderResponse);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderMapper.toDto(any(Order.class))).thenReturn(testOrderResponse);

        // Act
        List<OrderResponse> result = orderService.findAll();

        // Assert
        assertEquals(expectedResponses.size(), result.size());
        assertEquals(expectedResponses.get(0).id(), result.get(0).id());
        verify(orderRepository).findAll();
        verify(orderMapper).toDto(testOrder);
    }

    @Test
    @DisplayName("findByUserId should return list of order responses for user")
    void findByUserId_ShouldReturnListOfOrderResponses() {
        // Arrange
        List<Order> orders = List.of(testOrder);
        List<OrderResponse> expectedResponses = List.of(testOrderResponse);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(orderRepository.findAllByUser(testUser)).thenReturn(orders);
        when(orderMapper.toDto(any(Order.class))).thenReturn(testOrderResponse);

        // Act
        List<OrderResponse> result = orderService.findByUserId(USER_ID);

        // Assert
        assertEquals(expectedResponses.size(), result.size());
        assertEquals(expectedResponses.get(0).id(), result.get(0).id());
        verify(userRepository).findById(USER_ID);
        verify(orderRepository).findAllByUser(testUser);
        verify(orderMapper).toDto(testOrder);
    }

    @Test
    @DisplayName("findByUserId should throw ResourceNotFoundException when user not found")
    void findByUserId_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.findByUserId(USER_ID),
                "No user with id: " + USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(orderRepository, never()).findAllByUser(any(User.class));
    }

    @Test
    @DisplayName("findById should return order response when order exists")
    void findById_ShouldReturnOrderResponse_WhenOrderExists() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDto(testOrder)).thenReturn(testOrderResponse);

        // Act
        OrderResponse result = orderService.findById(ORDER_ID);

        // Assert
        assertEquals(testOrderResponse, result);
        verify(orderRepository).findById(ORDER_ID);
        verify(orderMapper).toDto(testOrder);
    }

    @Test
    @DisplayName("findById should throw ResourceNotFoundException when order not found")
    void findById_ShouldThrowResourceNotFoundException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.findById(ORDER_ID),
                "No order with id: " + ORDER_ID);
        verify(orderRepository).findById(ORDER_ID);
        verify(orderMapper, never()).toDto(any(Order.class));
    }

    @Test
    @DisplayName("updateOrderStatus should update status when valid transition")
    void updateOrderStatus_ShouldUpdateStatus_WhenValidTransition() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDto(any(Order.class))).thenReturn(testOrderResponse);

        // Act
        OrderResponse result = orderService.updateOrderStatus(ORDER_ID, updateRequest);

        // Assert
        assertEquals(testOrderResponse, result);
        assertEquals(OrderStatus.SHIPPED, testOrder.getStatus());
        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository).save(testOrder);
        verify(orderMapper).toDto(testOrder);
    }

    @Test
    @DisplayName("updateOrderStatus should throw ResourceNotFoundException when order not found")
    void updateOrderStatus_ShouldThrowResourceNotFoundException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(ORDER_ID, updateRequest),
                "Order not found with id: " + ORDER_ID);
        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("updateOrderStatus should throw InvalidOrderStatusTransitionException for invalid transition")
    void updateOrderStatus_ShouldThrowInvalidOrderStatusTransitionException_WhenInvalidTransition() {
        // Arrange
        testOrder.setStatus(OrderStatus.DELIVERED);
        UpdateOrderStatusRequest invalidRequest = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.updateOrderStatus(ORDER_ID, invalidRequest),
                "Cannot transition from DELIVERED to CONFIRMED");
        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("cancelOrder should cancel order and restore stock")
    void cancelOrder_ShouldCancelOrderAndRestoreStock() {
        // Arrange
        testProduct.setStockQuantity(98); // Simulate previous decrement
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDto(any(Order.class))).thenReturn(testOrderResponse);

        // Act
        OrderResponse result = orderService.cancelOrder(ORDER_ID);

        // Assert
        assertEquals(testOrderResponse, result);
        assertEquals(OrderStatus.CANCELED, testOrder.getStatus());
        assertEquals(100, testProduct.getStockQuantity()); // 98 + 2
        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository).save(testOrder);
        verify(orderMapper).toDto(testOrder);
    }

    @Test
    @DisplayName("cancelOrder should throw ResourceNotFoundException when order not found")
    void cancelOrder_ShouldThrowResourceNotFoundException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.cancelOrder(ORDER_ID),
                "Order not found with id: " + ORDER_ID);
        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("isOwner should return true when user owns the order")
    void isOwner_ShouldReturnTrue_WhenUserOwnsOrder() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(testOrder));

        // Act
        boolean result = orderService.isOwner(ORDER_ID, USER_ID);

        // Assert
        assertTrue(result);
        verify(orderRepository).findById(ORDER_ID);
    }

    @Test
    @DisplayName("isOwner should return false when user does not own the order")
    void isOwner_ShouldReturnFalse_WhenUserDoesNotOwnOrder() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(testOrder));

        // Act
        boolean result = orderService.isOwner(ORDER_ID, 2L);

        // Assert
        assertFalse(result);
        verify(orderRepository).findById(ORDER_ID);
    }

    @Test
    @DisplayName("isOwner should return false when order not found")
    void isOwner_ShouldReturnFalse_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // Act
        boolean result = orderService.isOwner(ORDER_ID, USER_ID);

        // Assert
        assertFalse(result);
        verify(orderRepository).findById(ORDER_ID);
    }
}
