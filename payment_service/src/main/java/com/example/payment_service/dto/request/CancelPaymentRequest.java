package com.example.payment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelPaymentRequest(

        @Size(
                max = 64,
                message = "Mã lý do không được vượt quá 64 ký tự"
        )
        String reasonCode,

        @NotBlank(message = "Lý do hủy không được để trống")
        @Size(
                max = 1000,
                message = "Lý do hủy không được vượt quá 1000 ký tự"
        )
        String reason

) {
}
