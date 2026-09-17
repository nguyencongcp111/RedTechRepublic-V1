package com.example.order_service.client;

import com.example.order_service.client.dto.ProductCatalogResponse;
import com.example.order_service.exception.DownstreamServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class ProductCatalogClient {
    private final RestClient restClient;

    public ProductCatalogClient(
            @Value("${services.product.base-url:http://localhost:8081}") String baseUrl
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ProductCatalogResponse getProduct(UUID productId) {
        try {
            ProductCatalogResponse response = restClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .body(ProductCatalogResponse.class);
            if (response == null) {
                throw new DownstreamServiceException("Product Service trả về response rỗng");
            }
            return response;
        } catch (RestClientException exception) {
            throw new DownstreamServiceException(
                    "Không thể xác minh sản phẩm " + productId, exception
            );
        }
    }
}
