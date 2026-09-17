package com.example.order_service.dto.request;

import com.example.order_service.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID userId,

        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z]{3}$",
                message = "Currency phải gồm 3 chữ cái"
        )
        String currency,

        @NotNull
        PaymentMethod paymentMethod,

        @NotEmpty
        List<@Valid CreateOrderItemRequest> items
) {
}
