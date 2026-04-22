package com.am9.okazx.service.impl;

import com.am9.okazx.dto.request.ProductRequest;
import com.am9.okazx.dto.response.ProductResponse;
import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.mapper.ProductMapper;
import com.am9.okazx.model.entity.Product;
import com.am9.okazx.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUpUsedData() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(19.99))
                .stockQuantity(100)
                .category("Test Category")
                .imageUrl("http://example.com/image.jpg")
                .active(true)
                .build();
        productRequest = new ProductRequest(
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(19.99),
                100,
                "Test Category",
                "http://example.com/image.jpg",
                true
        );
        productResponse = new ProductResponse(
                1L,
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(19.99),
                100,
                "Test Category",
                "http://example.com/image.jpg"
        );
    }

    @Test
    @DisplayName("find all products should return list of product response")
    void findAll_ShouldReturnListOfProductResponse() {
        // Given
        List<Product> products = List.of(product);
        List<ProductResponse> expectedResponses = List.of(productResponse);
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toDto(any(Product.class))).thenReturn(productResponse);
        // When
        List<ProductResponse> result = productService.findAll();
        // Then
        assertEquals(expectedResponses.size(), result.size());
        assertEquals(expectedResponses.get(0), result.get(0));
        verify(productRepository).findAll();
        verify(productMapper).toDto(products.get(0));
    }

    @Test
    @DisplayName("find by id should return product response")
    void findById_ShouldReturnProductResponse() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(productMapper.toDto(any(Product.class))).thenReturn(productResponse);
        // Act
        ProductResponse result = productService.findById(1L);
        // Assert
        assertEquals(productResponse, result);
        verify(productRepository).findById(1L);
        verify(productMapper).toDto(product);
    }

    @Test
    @DisplayName("find by id not found should throw ResourceNotFoundException")
    void findById_NotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(productRepository.findById(2L)).thenReturn(java.util.Optional.empty());
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.findById(2L));
        verify(productRepository).findById(2L);
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    @DisplayName("search product should return list of product response")
    void searchProducts_ShouldReturnListOfProductResponse() {
        // Given
        List<Product> products = List.of(product);
        List<ProductResponse> expectedResponses = List.of(productResponse);
        when(productRepository.searchProducts(any(String.class))).thenReturn(products);
        when(productMapper.toDto(any(Product.class))).thenReturn(productResponse);
        // When
        List<ProductResponse> result = productService.searchProducts("Test");
        // Then
        assertEquals(expectedResponses.size(), result.size());
        assertEquals(expectedResponses.get(0), result.get(0));
        verify(productRepository).searchProducts("Test");
        verify(productMapper).toDto(products.get(0));
    }

    @Test
    @DisplayName("create product should return created product response")
    void create_ShouldReturnCreatedProductResponse() {
        // Arrange
        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(any(Product.class))).thenReturn(productResponse);
        // Act
        ProductResponse result = productService.create(productRequest);
        // Assert
        assertEquals(productResponse, result);
        verify(productMapper).toEntity(productRequest);
        verify(productRepository).save(product);
        verify(productMapper).toDto(product);
    }

    @Test
    @DisplayName("update product should return updated product response")
    void update_ShouldReturnUpdatedProductResponse() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(productMapper.toDto(any(Product.class))).thenReturn(productResponse);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        // Act
        ProductResponse result = productService.update(1L, productRequest);
        // Assert
        assertEquals(productResponse, result);
        verify(productRepository).findById(1L);
        verify(productMapper).updateEntityFromRequest(productRequest, product);
        verify(productRepository).save(product);
        verify(productMapper).toDto(product);
    }

    @Test
    @DisplayName("update not found product should throw ResourceNotFoundException")
    void update_NotFoundProduct_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(productRepository.findById(2L)).thenReturn(java.util.Optional.empty());
        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> productService.update(2L, productRequest));

        verify(productRepository).findById(2L);
    }

    @Test
    @DisplayName("delete product by id should delete product and return nothing")
    void delete_existing_product_shouldDeleteProductAndReturnNothing() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        // Act
        productService.delete(1L);
        // Assert
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete not found product should throw ResourceNotFoundException")
    void delete_notFound_product_shouldThrowResourceNotFoundException() {
        // Given
        when(productRepository.existsById(2L)).thenReturn(false);
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> productService.delete(2L));
        verify(productRepository).existsById(2L);
        verify(productRepository, never()).deleteById(any(Long.class));
    }



}
