package com.example.product_service.controller;

import com.example.product_service.dto.request.CreateCategoryRequest;
import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.request.UpdateProductRequest;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.dto.response.PageResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.service.CategoryService;
import com.example.product_service.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final com.example.product_service.security.CurrentUser currentUser;

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(
            @Valid @RequestBody CreateProductRequest request,
            jakarta.servlet.http.HttpServletRequest http
    ) {
        // Người đăng luôn lấy từ token, không nhận từ body.
        return new ResponseEntity<>(
                productService.addProduct(request, currentUser.requirePublisher(http)), HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) com.example.product_service.enums.ProductStatus status,
            @RequestParam(required = false) String poster,
            @RequestParam(required = false) UUID shopId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        PageResponse<ProductResponse> products =
                productService.search(keyword, categoryId, status, poster, shopId, pageable);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable UUID id
    ) {
        return new ResponseEntity<>(
                productService.getProductById(id), HttpStatus.OK
        );
    }

    @GetMapping("/by-sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(
            @PathVariable String sku
    ) {
        return new ResponseEntity<>(
                productService.getProductBySku(sku), HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> editProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request,
            jakarta.servlet.http.HttpServletRequest http
    ) {
        return new ResponseEntity<>(
                productService.editProduct(request, id, currentUser.requirePublisher(http)), HttpStatus.OK
        );
    }
}
