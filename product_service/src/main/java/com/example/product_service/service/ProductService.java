package com.example.product_service.service;

import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.request.UpdateProductRequest;
import com.example.product_service.dto.response.PageResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponse addProduct(CreateProductRequest request);

    PageResponse<ProductResponse> getAllProduct(int page, int size, String sortBy, Sort.Direction sortDirection);

    PageResponse<ProductResponse> search(String keyword, Pageable pageable);

    ProductResponse getProductById (UUID id);

    ProductResponse getProductBySku (String sku);

    ProductResponse editProduct(UpdateProductRequest request, UUID id);
}
