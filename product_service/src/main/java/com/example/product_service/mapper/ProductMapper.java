package com.example.product_service.mapper;

import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.request.UpdateProductRequest;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.entity.Category;
import com.example.product_service.entity.Product;
import com.example.product_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    public Product addToEntity(CreateProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .sku(request.sku().trim().toUpperCase(Locale.ROOT))
                .name(request.name().trim())
                .description(request.description())
                .price(request.price())
                .currency(request.currency().trim().toUpperCase(Locale.ROOT))
                .build();
    }

    public void updateToEntity(UpdateProductRequest request, Product product) {
        if (request == null || product == null) {
            return;
        }

        if (request.name() != null) {
            product.setName(request.name().trim());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.status() != null) {
            product.setStatus(request.status());
        }
    }

    public ProductResponse toDTO(Product product) {
        if (product == null) {
            return null;
        }
        Category category = product.getCategory();

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                category == null
                        ? null
                        : new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getVersion(),
                product.getImageUrl()
        );
    }
}
