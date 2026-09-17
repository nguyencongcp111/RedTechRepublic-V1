package com.example.payment_service.outbox;

import com.example.payment_service.entity.Payment;
import com.example.payment_service.messaging.PaymentStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentOutboxService {
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public void recordStatus(Payment payment) {
        try {
            String payload = objectMapper.writeValueAsString(new PaymentStatusEvent(
                    payment.getId(), payment.getOrderId(), payment.getStatus().name(), Instant.now()
            ));
            repository.save(OutboxEvent.builder()
                    .routingKey("payment.status." + payment.getStatus().name().toLowerCase())
                    .payload(payload)
                    .createdAt(Instant.now())
                    .build());
        } catch (JacksonException exception) {
            throw new IllegalStateException("Không thể tạo payment event", exception);
        }
    }
}
