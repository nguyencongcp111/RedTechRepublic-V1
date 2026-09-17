package com.example.auth_service.dto.response;

public record TokenResponseDTO(
        String accessToken,
        String refreshToken
) {
}
