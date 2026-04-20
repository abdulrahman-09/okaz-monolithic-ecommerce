package com.am9.okazx.service;

import com.am9.okazx.dto.request.ProductRequest;
import com.am9.okazx.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> findAll();
    ProductResponse findById(Long id);
    ProductResponse create(ProductRequest productRequest);
    ProductResponse update(Long id, ProductRequest productRequest);
    void delete(Long id);
    List<ProductResponse> searchProducts(String keyword);


}
