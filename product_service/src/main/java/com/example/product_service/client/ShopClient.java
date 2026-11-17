package com.example.product_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/** Looks up shop ownership in Inventory Service; product_service never reads that database. */
@Component
public class ShopClient {
    private final RestClient restClient;

    public ShopClient(@Value("${services.inventory.base-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public record ShopSummary(UUID id, String name, String ownerUsername) {}

    public ShopSummary getShop(UUID shopId) {
        try {
            ShopSummary shop = restClient.get().uri("/api/shops/{id}", shopId).retrieve().body(ShopSummary.class);
            if (shop == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Inventory Service trả về rỗng");
            return shop;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy gian hàng");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không kiểm tra được gian hàng", ex);
        }
    }
}
