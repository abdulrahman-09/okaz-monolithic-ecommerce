package com.am9.okazx.service.impl;

import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.mapper.ProductMapper;
import com.am9.okazx.dto.request.ProductRequest;
import com.am9.okazx.dto.response.ProductResponse;
import com.am9.okazx.model.entity.Product;
import com.am9.okazx.repository.ProductRepository;
import com.am9.okazx.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    final private ProductRepository productRepository;
    final private ProductMapper productMapper;

    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public ProductResponse create(ProductRequest productRequest) {
        return productMapper.toDto(
                productRepository.save(productMapper.toEntity(productRequest))
        );
    }

    @Override
    public ProductResponse update(Long id, ProductRequest updatedProductDto) {
        Product productToUpdate = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productMapper.updateEntityFromRequest(updatedProductDto, productToUpdate);
        return productMapper.toDto(
                productRepository.save(productToUpdate)
        );
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
