package com.example.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank
        @Size(max = 255, message = "Tên không được quá 255 ký tự")
        String name,

        @Size(max = 1500, message = "Mô tả không được quá 1500 ký tự")
        String description
) {
}
