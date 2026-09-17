package com.example.inventory_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ReservationExpirationJob {
    private final InventoryReservationService inventoryReservationService;

    @Scheduled(fixedDelayString = "${inventory.reservation.expiration-interval-ms:30000}")
    public void expireReservations() {
        inventoryReservationService.expireReservations(Instant.now(), 100);
    }
}
