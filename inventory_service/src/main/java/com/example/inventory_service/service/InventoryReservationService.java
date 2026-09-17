package com.example.inventory_service.service;

import com.example.inventory_service.dto.request.CreateInventoryReservationRequest;
import com.example.inventory_service.dto.request.SetStatusRequest;
import com.example.inventory_service.dto.response.InventoryReservationResponse;
import com.example.inventory_service.enums.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InventoryReservationService {
    InventoryReservationResponse addReservation(String idempotencyKey, CreateInventoryReservationRequest request);

    List<InventoryReservationResponse> getReservationsByOrderId(UUID orderId);

    InventoryReservationResponse statusSet(UUID id, SetStatusRequest request);

    int expireReservations(Instant now, int batchSize);
}
