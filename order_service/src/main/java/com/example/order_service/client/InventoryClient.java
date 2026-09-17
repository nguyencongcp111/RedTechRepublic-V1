package com.example.order_service.client;

import com.example.order_service.client.dto.InventoryReservationRequest;
import com.example.order_service.client.dto.InventoryReservationResponse;
import com.example.order_service.client.dto.InventoryStatusRequest;
import com.example.order_service.exception.DownstreamServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(
            @Value("${services.inventory.base-url:http://localhost:8082}") String baseUrl
    ) {
        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public InventoryReservationResponse reserve(
            UUID orderId,
            UUID orderItemId,
            String sku,
            int quantity
    ) {
        try {
            InventoryReservationResponse response = restClient.post()
                    .uri("/api/inventory/reservation")
                    .header(
                            "Idempotency-Key",
                            "order:" + orderId + ":item:" + orderItemId
                    )
                    .body(new InventoryReservationRequest(
                            orderId,
                            sku,
                            quantity
                    ))
                    .retrieve()
                    .body(InventoryReservationResponse.class);

            if (response == null) {
                throw new DownstreamServiceException(
                        "Inventory Service trả response rỗng"
                );
            }

            return response;

        } catch (RestClientException exception) {
            throw new DownstreamServiceException(
                    "Không thể giữ tồn kho cho SKU " + sku,
                    exception
            );
        }
    }

    public List<InventoryReservationResponse> getReservationsByOrder(
            UUID orderId
    ) {
        try {
            List<InventoryReservationResponse> response = restClient.get()
                    .uri(
                            "/api/inventory/reservation/by-order/{orderId}",
                            orderId
                    )
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null) {
                throw new DownstreamServiceException(
                        "Inventory Service trả response rỗng"
                );
            }

            return response;

        } catch (RestClientException exception) {
            throw new DownstreamServiceException(
                    "Không thể lấy reservations của order " + orderId,
                    exception
            );
        }
    }

    public void transition(
            UUID reservationId,
            String status
    ) {
        try {
            restClient.patch()
                    .uri(
                            "/api/inventory/reservation/{id}/status",
                            reservationId
                    )
                    .body(new InventoryStatusRequest(status))
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException exception) {
            throw new DownstreamServiceException(
                    "Không thể chuyển reservation "
                            + reservationId
                            + " sang "
                            + status,
                    exception
            );
        }
    }
}
