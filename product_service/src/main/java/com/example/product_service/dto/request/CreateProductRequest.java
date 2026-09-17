package com.example.product_service.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateProductRequest(
        @NotBlank
        @Size(max=64)
        @Pattern(
                regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$",
                message = "SKU chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới." +
                        "Thử nhập lại"
        )
        String sku,

        @NotBlank
        @Size(max = 255, message = "Tên không được quá 255 ký tự")
        String name,

        @Size(max = 1500, message = "Mô tả không được quá 1500 ký tự")
        String description,

        @NotNull
        @PositiveOrZero
        Long price,

        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z]{3}$",
                message = "Đơn vị tiền phải là ba chữ cái"
        )
        String currency,

        @NotNull
        UUID categoryId
) {

}
