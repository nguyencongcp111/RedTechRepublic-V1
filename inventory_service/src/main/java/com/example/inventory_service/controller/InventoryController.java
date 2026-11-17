package com.example.inventory_service.controller;

import com.example.inventory_service.dto.request.CreateInventoryItemRequest;
import com.example.inventory_service.dto.request.AdjustInventoryRequest;
import com.example.inventory_service.dto.request.CreateInventoryReservationRequest;
import com.example.inventory_service.dto.request.SetStatusRequest;
import com.example.inventory_service.dto.response.InventoryItemResponse;
import com.example.inventory_service.dto.response.InventoryReservationResponse;
import com.example.inventory_service.service.InventoryItemService;
import com.example.inventory_service.service.InventoryReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
@Validated
public class InventoryController {
    private final InventoryItemService inventoryItemService;
    private final InventoryReservationService inventoryReservationService;
    private final com.example.inventory_service.security.CurrentUser currentUser;
    private final com.example.inventory_service.client.ProductOwnerClient productOwners;

    /** ADMIN quản lý mọi kho; SELLER chỉ tạo/điều chỉnh kho cho sản phẩm do mình đăng. */
    private void requireStockOwner(jakarta.servlet.http.HttpServletRequest http,
                                   java.util.function.Supplier<com.example.inventory_service.client.ProductOwnerClient.ProductOwner> product) {
        var principal = currentUser.requirePublisher(http);
        if (principal.isAdmin()) return;
        if (!principal.username().equals(product.get().poster()))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chỉ được quản lý kho của sản phẩm do mình đăng");
    }

    @PostMapping("/stock")
    public ResponseEntity<InventoryItemResponse> addItem (
            @Valid @RequestBody CreateInventoryItemRequest request,
            jakarta.servlet.http.HttpServletRequest http
    ) {
        requireStockOwner(http, () -> {
            var product = productOwners.byId(request.productId());
            if (!product.sku().equalsIgnoreCase(request.sku().trim()))
                throw new IllegalStateException("SKU không khớp với sản phẩm");
            return product;
        });
        return new ResponseEntity<>(
                inventoryItemService.addInventoryItem(request), HttpStatus.CREATED
        );
    }

    @GetMapping("/{sku}")
    public ResponseEntity<InventoryItemResponse> getItem (
            @PathVariable String sku
    ) {
        return new ResponseEntity<>(
                inventoryItemService.getInventoryItemBySku(sku), HttpStatus.OK
        );
    }

    @PostMapping("/reservation")
    public ResponseEntity<InventoryReservationResponse> addReservation (
            @RequestHeader("Idempotency-Key") @Size(min = 1, max = 128) String idempotencyKey,
            @Valid @RequestBody CreateInventoryReservationRequest request
    ) {
        return new ResponseEntity<>(
                inventoryReservationService.addReservation(idempotencyKey, request), HttpStatus.CREATED
        );
    }

    @PatchMapping("/reservation/{id}/status")
    public ResponseEntity<InventoryReservationResponse> editStatus (
            @PathVariable UUID id,
            @Valid @RequestBody SetStatusRequest request
    ) {
        return new ResponseEntity<>(
                inventoryReservationService.statusSet(id, request), HttpStatus.OK
        );
    }

    @PatchMapping("/stock/{sku}")
    public ResponseEntity<InventoryItemResponse> adjustItem(
            @PathVariable String sku,
            @Valid @RequestBody AdjustInventoryRequest request,
            jakarta.servlet.http.HttpServletRequest http
    ) {
        requireStockOwner(http, () -> productOwners.bySku(sku));
        return ResponseEntity.ok(inventoryItemService.adjustInventory(sku, request));
    }

    @GetMapping("/reservation/by-order/{orderId}")
    public ResponseEntity<List<InventoryReservationResponse>> getByOrder(
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(
                inventoryReservationService.getReservationsByOrderId(orderId)
        );
    }
}
