package com.am9.okazx.controller;



import com.am9.okazx.dto.request.ProductRequest;
import com.am9.okazx.dto.response.PageResponse;
import com.am9.okazx.dto.response.ProductResponse;
import com.am9.okazx.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product catalogue — public reads, ADMIN writes")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all products")
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(value = "pageNo", defaultValue = "0") @Min(0) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id,asc") String sortBy
    ){
        return ResponseEntity.ok(productService.findAll(pageNo, pageSize, sortBy));
    }

    @PostMapping
    @Operation(summary = "Add product")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest productRequest){
        ProductResponse createdProduct = productService.create(productRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.id())
                .toUri();
        return ResponseEntity.created(location).body(createdProduct);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id){
        ProductResponse productResponse = productService.findById(id);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword){
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update products by id")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody @Valid ProductRequest productRequest){
        return ResponseEntity.ok(
                productService.update(id, productRequest)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete products by id")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
