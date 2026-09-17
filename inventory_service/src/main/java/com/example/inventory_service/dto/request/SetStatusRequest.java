package com.example.inventory_service.dto.request;

import com.example.inventory_service.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record SetStatusRequest(
        @NotNull ReservationStatus status
) {
}
