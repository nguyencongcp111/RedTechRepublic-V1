package com.example.product_service.service.impl;

import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.request.UpdateProductRequest;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.dto.response.PageResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.entity.Product;
import com.example.product_service.enums.ProductStatus;
import com.example.product_service.exception.ResourceNotFoundException;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse addProduct(CreateProductRequest request) {
        Product product = productMapper.addToEntity(request);

        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProduct(int page, int size, String sortBy, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ProductResponse> productResponsePage = productRepository.findAll(pageable)
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getSku(),
                        product.getName(),
                        product.getCategory() != null
                                ? new CategoryResponse(
                                product.getCategory().getId(),
                                product.getCategory().getName(),
                                product.getCategory().getDescription()
                        )
                                : null,
                        product.getDescription(),
                        product.getPrice(),
                        product.getCurrency(),
                        product.getStatus(),
                        product.getCreatedAt(),
                        product.getUpdatedAt(),
                        product.getVersion()
                ));

        return new PageResponse<>(
                productResponsePage.getContent(),
                productResponsePage.getNumber(),
                productResponsePage.getSize(),
                productResponsePage.getTotalElements(),
                productResponsePage.getTotalPages(),
                productResponsePage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, Pageable pageable) {
        Page<Product> page = (keyword == null || keyword.isBlank())
                ? productRepository.findAll(pageable)
                :
                productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);

        Page<ProductResponse> responsePage = page.map(product ->
                new ProductResponse(
                        product.getId(),
                        product.getSku(),
                        product.getName(),
                        product.getCategory() != null
                                ? new CategoryResponse(
                                product.getCategory().getId(),
                                product.getCategory().getName(),
                                product.getCategory().getDescription()
                        )
                                : null,
                        product.getDescription(),
                        product.getPrice(),
                        product.getCurrency(),
                        product.getStatus(),
                        product.getCreatedAt(),
                        product.getUpdatedAt(),
                        product.getVersion()
                )
        );

        return new PageResponse<>(
                responsePage.getContent(),
                responsePage.getNumber(),
                responsePage.getSize(),
                responsePage.getTotalElements(),
                responsePage.getTotalPages(),
                responsePage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById (UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toDTO)
                .orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy sản phẩm")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku (String sku) {
        return productRepository.findBySku(sku.trim().toUpperCase(Locale.ROOT))
                .map(productMapper::toDTO)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy sản phẩm")
                );
    }

    @Override
    @Transactional
    public ProductResponse editProduct(UpdateProductRequest request, UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        if (product.getStatus() == ProductStatus.ARCHIVED) {
            throw new com.example.product_service.exception.InvalidProductStateException(
                    "Sản phẩm đã được lưu trữ, không thể chỉnh sửa"
            );
        }

        productMapper.updateToEntity(request, product);
        return productMapper.toDTO(product);
    }
}
