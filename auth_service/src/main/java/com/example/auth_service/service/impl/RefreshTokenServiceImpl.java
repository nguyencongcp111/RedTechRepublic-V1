package com.example.auth_service.service.impl;

import com.example.auth_service.dto.response.TokenResponseDTO;
import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.jwt.JwtUtil;
import com.example.auth_service.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt-refresh-expired}")
    private long refreshTokenExpir;

    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Tài khoản không tồn tại"));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpir))
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(String token) {
        RefreshToken existToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy refresh token"));

        if (existToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existToken);

            throw new NoSuchElementException("Refresh token đã hết hạn");
        }

        return existToken;
    }

    @Transactional
    public TokenResponseDTO refreshToken(String requestToken) {

        RefreshToken oldRefreshToken = verifyExpiration(requestToken);

        User user = oldRefreshToken.getUser();

        refreshTokenRepository.delete(oldRefreshToken);

        String newAccessToken = jwtUtil.generateToken(user);

        RefreshToken newRefreshToken =
                createRefreshToken(user.getId());

        return new TokenResponseDTO(
                newAccessToken,
                newRefreshToken.getToken()
        );
    }


}