package com.example.inventory_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/** Hỏi Product Service ai là người đăng sản phẩm, để SELLER chỉ quản lý kho hàng của mình. */
@Component
public class ProductOwnerClient {
    private final RestClient restClient;

    public ProductOwnerClient(@Value("${services.product.base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public record ProductOwner(UUID id, String sku, String poster) {}

    public ProductOwner byId(UUID productId) {
        return fetch("/api/products/{id}", productId);
    }

    public ProductOwner bySku(String sku) {
        return fetch("/api/products/by-sku/{sku}", sku);
    }

    private ProductOwner fetch(String uri, Object variable) {
        try {
            ProductOwner product = restClient.get().uri(uri, variable).retrieve().body(ProductOwner.class);
            if (product == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product Service trả về rỗng");
            return product;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy sản phẩm");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không kiểm tra được sản phẩm", ex);
        }
    }
}
