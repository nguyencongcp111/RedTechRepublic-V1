package com.example.payment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmPaymentRequest(

        @NotBlank(message = "Provider reference không được để trống")
        @Size(
                max = 255,
                message = "Provider reference không được vượt quá 255 ký tự"
        )
        String providerReference

) {
}