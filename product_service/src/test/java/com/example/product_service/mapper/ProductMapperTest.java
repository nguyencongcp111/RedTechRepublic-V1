package com.example.product_service.mapper;

import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.entity.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductMapperTest {
    private final ProductMapper mapper = new ProductMapper();

    @Test
    void normalizesSkuAndCurrency() {
        Product product = mapper.addToEntity(
                new CreateProductRequest(" sku-1 ", " Product ", null, 100L, "vnd")
        );
        assertEquals("SKU-1", product.getSku());
        assertEquals("VND", product.getCurrency());
        assertEquals("Product", product.getName());
    }
}
