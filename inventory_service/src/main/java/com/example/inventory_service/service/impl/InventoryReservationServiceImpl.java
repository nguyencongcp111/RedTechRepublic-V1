package com.example.inventory_service.service.impl;

import com.example.inventory_service.dto.request.CreateInventoryReservationRequest;
import com.example.inventory_service.dto.request.SetStatusRequest;
import com.example.inventory_service.dto.response.InventoryReservationResponse;
import com.example.inventory_service.entity.InventoryItem;
import com.example.inventory_service.entity.InventoryReservation;
import com.example.inventory_service.enums.ReservationStatus;
import com.example.inventory_service.exception.IdempotencyKeyConflictException;
import com.example.inventory_service.exception.InsufficientStockException;
import com.example.inventory_service.exception.InvalidReservationStateException;
import com.example.inventory_service.exception.ResourceNotFoundException;
import com.example.inventory_service.mapper.InventoryReservationMapper;
import com.example.inventory_service.repository.InventoryItemRepository;
import com.example.inventory_service.repository.InventoryReservationRepository;
import com.example.inventory_service.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryReservationServiceImpl implements InventoryReservationService {
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryReservationMapper inventoryReservationMapper;

    @Override
    @Transactional
    public InventoryReservationResponse addReservation(
            String idempotencyKey,
            CreateInventoryReservationRequest request) {

        InventoryReservation existing =
                inventoryReservationRepository
                        .findByIdempotencyKey(idempotencyKey)
                        .orElse(null);

        if (existing != null) {
            validateSameRequest(existing, request);
            return inventoryReservationMapper.toDTO(existing);
        }

        String normalizedSku =
                request.sku().trim().toUpperCase(Locale.ROOT);

        InventoryItem inventoryItem = inventoryItemRepository
                .findBySkuForUpdate(normalizedSku)
                .orElseThrow(() ->
                        new ResourceNotFoundException(normalizedSku)
                );

        existing = inventoryReservationRepository.findByIdempotencyKey(idempotencyKey)
                .orElse(null);
        if (existing != null) {
            validateSameRequest(existing, request);
            return inventoryReservationMapper.toDTO(existing);
        }

        if (inventoryItem.available() < request.quantity()) {
            throw new InsufficientStockException(
                    "Số sản phẩm trong kho không đủ"
            );
        }

        inventoryItem.reserve(request.quantity());

        InventoryReservation reservation =
                inventoryReservationMapper.addToEntity(
                        request,
                        inventoryItem,
                        idempotencyKey,
                        Instant.now().plus(15, ChronoUnit.MINUTES)
                );

        InventoryReservation saved =
                inventoryReservationRepository.save(reservation);

        return inventoryReservationMapper.toDTO(saved);
    }

    private void validateSameRequest(
            InventoryReservation existing,
            CreateInventoryReservationRequest request
    ) {
        String normalizedSku =
                request.sku().trim().toUpperCase(Locale.ROOT);

        boolean sameOrder =
                existing.getOrderId().equals(request.orderId());

        boolean sameSku =
                existing.getInventoryItem()
                        .getSku()
                        .equalsIgnoreCase(normalizedSku);

        boolean sameQuantity =
                existing.getQuantity() == request.quantity();

        if (!sameOrder || !sameSku || !sameQuantity) {
            throw new IdempotencyKeyConflictException(
                    "Idempotency-Key đã được sử dụng cho một request khác"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryReservationResponse> getReservationsByOrderId(UUID orderId) {
        return inventoryReservationRepository.findAllByOrderId(orderId)
                .stream()
                .map(inventoryReservationMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public InventoryReservationResponse statusSet(UUID id, SetStatusRequest request) {
        InventoryReservation reservation = inventoryReservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không thể tìm thấy reservation"));

        ReservationStatus currentStatus = reservation.getStatus();
        ReservationStatus targetStatus = request.status();

        if (currentStatus == targetStatus) {
            return inventoryReservationMapper.toDTO(reservation);
        }

        if (currentStatus != ReservationStatus.ACTIVE) {
            throw new InvalidReservationStateException(
                    "Không thể chuyển reservation từ "
                            + currentStatus + " sang " + targetStatus
            );
        }

        InventoryItem inventoryItem = inventoryItemRepository
                .findByIdForUpdate(reservation.getInventoryItem().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không thể tìm thấy inventory item"));
        int quantity = reservation.getQuantity();

        switch (targetStatus) {
            case CONFIRMED -> {
                inventoryItem.confirm(quantity);
            }

            case RELEASED, EXPIRED -> inventoryItem.release(quantity);

            default -> throw new InvalidReservationStateException(
                    "Không thể chuyển reservation sang " + targetStatus
            );
        }

        reservation.setStatus(targetStatus);

        return inventoryReservationMapper.toDTO(reservation);
    }

    @Override
    @Transactional
    public int expireReservations(Instant now, int batchSize) {
        List<InventoryReservation> expired =
                inventoryReservationRepository.findExpiredForUpdate(now, batchSize);

        for (InventoryReservation reservation : expired) {
            if (reservation.getStatus() == ReservationStatus.ACTIVE) {
                InventoryItem item = inventoryItemRepository
                        .findByIdForUpdate(reservation.getInventoryItem().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không thể tìm thấy inventory item"
                        ));
                item.release(reservation.getQuantity());
                reservation.setStatus(ReservationStatus.EXPIRED);
            }
        }

        return expired.size();
    }
}
