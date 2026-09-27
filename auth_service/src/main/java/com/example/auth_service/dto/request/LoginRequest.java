package com.example.auth_service.dto.request;

public record LoginRequest(
        @jakarta.validation.constraints.NotBlank String username,
        @jakarta.validation.constraints.NotBlank String password
) {
}
