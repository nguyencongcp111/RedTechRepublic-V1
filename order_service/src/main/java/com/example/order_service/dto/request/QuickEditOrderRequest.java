package com.example.order_service.dto.request;

import com.example.order_service.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

/**
 * Sửa nhanh đơn hàng (ADMIN). Trường null = giữ nguyên.
 * - paymentMethod: khi đơn chưa CONFIRMED/CANCELLED/FAILED.
 * - items: đổi số lượng từng dòng, chỉ khi đơn còn PENDING (chưa giữ hàng).
 */
public record QuickEditOrderRequest(
        PaymentMethod paymentMethod,
        List<@Valid ItemQuantity> items
) {
    public record ItemQuantity(@NotNull UUID itemId, @Positive @Max(10_000) int quantity) {}
}
