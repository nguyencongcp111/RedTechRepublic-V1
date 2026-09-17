package com.example.auth_service.service;

import com.example.auth_service.entity.RefreshToken;

import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(UUID userId);

    RefreshToken verifyExpiration(String token);

}
