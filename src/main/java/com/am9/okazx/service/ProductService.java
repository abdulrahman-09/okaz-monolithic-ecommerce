package com.am9.okazx.service;

import com.am9.okazx.model.dto.ProductRequest;
import com.am9.okazx.model.dto.ProductResponse;
import com.am9.okazx.model.dto.UserRequest;
import com.am9.okazx.model.dto.UserResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> findAll();
    ProductResponse findById(Long id);
    ProductResponse create(ProductRequest productRequest);
    ProductResponse update(Long id, ProductRequest productRequest);
    void delete(Long id);
    List<ProductResponse> searchProducts(String keyword);


}
