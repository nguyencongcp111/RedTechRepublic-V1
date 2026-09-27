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
    private final String jwtSecret;

    public OrderClient(
            @Value("${services.order.base-url:http://localhost:8083}") String baseUrl,
            @Value("${JWT_SECRET}") String jwtSecret
    ) {
        restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.jwtSecret = jwtSecret;
    }

    public OrderSummaryResponse getOrder(UUID orderId) {
        try {
            OrderSummaryResponse response = restClient.get()
                    .uri("/api/orders/{id}", orderId)
                    .headers(h -> h.setBearerAuth(serviceToken()))
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
    private String serviceToken() {
        try {
            var claims = new com.nimbusds.jwt.JWTClaimsSet.Builder().subject("payment-service")
                    .claim("role", "SERVICE").issueTime(new java.util.Date())
                    .expirationTime(java.util.Date.from(java.time.Instant.now().plusSeconds(60))).build();
            var jwt = new com.nimbusds.jwt.SignedJWT(new com.nimbusds.jose.JWSHeader(com.nimbusds.jose.JWSAlgorithm.HS256), claims);
            jwt.sign(new com.nimbusds.jose.crypto.MACSigner(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (com.nimbusds.jose.JOSEException ex) { throw new IllegalStateException("Không tạo được service token", ex); }
    }
}
