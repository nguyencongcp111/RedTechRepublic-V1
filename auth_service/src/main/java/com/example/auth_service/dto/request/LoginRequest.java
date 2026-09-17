package com.example.auth_service.dto.request;

public record LoginRequest(
        String username,
        String password
) {
}
