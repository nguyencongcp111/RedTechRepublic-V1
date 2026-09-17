package com.example.payment_service.client;

import com.example.payment_service.client.dto.OrderSummaryResponse;
import com.example.payment_service.exception.DownstreamServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class OrderClient {
    private final RestClient restClient;

    public OrderClient(
            @Value("${services.order.base-url:http://localhost:8083}") String baseUrl
    ) {
        restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public OrderSummaryResponse getOrder(UUID orderId) {
        try {
            OrderSummaryResponse response = restClient.get()
                    .uri("/api/orders/{id}", orderId)
                    .retrieve()
                    .body(OrderSummaryResponse.class);
            if (response == null) {
                throw new DownstreamServiceException("Order Service trả response rỗng");
            }
            return response;
        } catch (RestClientException exception) {
            throw new DownstreamServiceException("Không thể xác minh order " + orderId, exception);
        }
    }
}
