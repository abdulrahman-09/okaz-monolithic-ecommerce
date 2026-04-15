package com.am9.okazx.controller;



import com.am9.okazx.model.dto.ProductRequest;
import com.am9.okazx.model.dto.ProductResponse;
import com.am9.okazx.model.dto.UserRequest;
import com.am9.okazx.model.dto.UserResponse;
import com.am9.okazx.service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(productService.findAll());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest){
        ProductResponse createdProduct = productService.create(productRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.id())
                .toUri();
        return ResponseEntity.created(location).body(createdProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id){
        ProductResponse productResponse = productService.findById(id);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword){
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductRequest productRequest){
        return ResponseEntity.ok(
                productService.update(id, productRequest)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
