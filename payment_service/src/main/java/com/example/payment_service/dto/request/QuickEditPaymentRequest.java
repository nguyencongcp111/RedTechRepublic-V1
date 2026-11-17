package com.example.payment_service.dto.request;

import com.example.payment_service.enums.PaymentMethod;
import jakarta.validation.constraints.Size;

/**
 * Sửa nhanh thanh toán (ADMIN), chỉ khi PENDING. Trường null = giữ nguyên;
 * providerReference rỗng = xóa mã đối soát.
 * Trạng thái vẫn đi qua confirm / cancel / refund để luồng đơn hàng - kho không lệch.
 */
public record QuickEditPaymentRequest(
        PaymentMethod paymentMethod,
        @Size(max = 255) String providerReference
) {}
