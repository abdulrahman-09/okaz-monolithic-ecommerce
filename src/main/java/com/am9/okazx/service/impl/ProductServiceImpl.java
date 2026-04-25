package com.am9.okazx.service.impl;

import com.am9.okazx.dto.response.PageResponse;
import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.mapper.ProductMapper;
import com.am9.okazx.dto.request.ProductRequest;
import com.am9.okazx.dto.response.ProductResponse;
import com.am9.okazx.model.entity.Product;
import com.am9.okazx.repository.ProductRepository;
import com.am9.okazx.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAll(int pageNo, int pageSize, String sortBy) {
        String[] sort = sortBy.split(",");
        Sort sortObj = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(productMapper::toDto)
                .toList();
        return new PageResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest productRequest) {
        return productMapper.toDto(
                productRepository.save(productMapper.toEntity(productRequest))
        );
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest updatedProductDto) {
        Product productToUpdate = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productMapper.updateEntityFromRequest(updatedProductDto, productToUpdate);
        return productMapper.toDto(
                productRepository.save(productToUpdate)
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
