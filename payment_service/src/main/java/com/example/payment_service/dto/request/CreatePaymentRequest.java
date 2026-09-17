package com.example.payment_service.dto.request;

import com.example.payment_service.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreatePaymentRequest(

        @NotNull(message = "Order ID không được để trống")
        UUID orderId,

        @Positive(message = "Số tiền thanh toán phải lớn hơn 0")
        long amount,

        @NotBlank(message = "Currency không được để trống")
        @Pattern(
                regexp = "^[A-Za-z]{3}$",
                message = "Currency phải là mã gồm 3 chữ cái"
        )
        String currency,

        @NotNull(message = "Phương thức thanh toán không được để trống")
        PaymentMethod paymentMethod

) {
}
