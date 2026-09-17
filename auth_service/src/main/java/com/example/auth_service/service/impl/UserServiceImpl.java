package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.LoginRequest;
import com.example.auth_service.dto.request.RegisterRequest;
import com.example.auth_service.dto.response.JWTResponse;
import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.Roles;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.config.JwtAuthenticationFilter;
import com.example.auth_service.security.jwt.JwtUtil;
import com.example.auth_service.service.RefreshTokenService;
import com.example.auth_service.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate redisTemplate;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    public User registerUser(RegisterRequest registerRequest) {
        boolean find = userRepository.existsByUsername(registerRequest.getUsername());
        if (find) {
            throw new RuntimeException("Username da ton tai");
        }

        String requestedRole = registerRequest.getRole() != null ?
                registerRequest.getRole().trim().toUpperCase() :
                "";

        String assignedRole = Roles.ADMIN.equals(requestedRole) ? Roles.ADMIN : Roles.USER;

        return userRepository.save(
                User.builder()
                        .username(registerRequest.getUsername())
                        .password(passwordEncoder.encode(registerRequest.getPassword()))
                        .role(assignedRole)
                        .build()
        );
    }

    @Override
    public JWTResponse loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> new BadCredentialsException(
                        "Không thể đăng nhập. Sai tài khoản hoặc mật khẩu"
                ));

        boolean matches = passwordEncoder.matches(
                loginRequest.password(),
                user.getPassword()
        );

        if (!matches) {
            throw new BadCredentialsException("Không thể đăng nhập. Sai tài khoản hoặc mật khẩu");
        }

        String token = jwtUtil.generateToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());



        return new JWTResponse(
                user.getUsername(),
                "Bearer",
                token,
                refreshToken.getToken()
        );
    }

    @Override
    public JWTResponse refreshAccessToken(String token) {
        RefreshToken refreshToken =
                refreshTokenService.verifyExpiration(token);

        User user = refreshToken.getUser();

        String newAccessToken =
                jwtUtil.generateToken(user);

        return JWTResponse.builder()
                .username(user.getUsername())
                .type("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public void logout(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        String token = authorization.substring(7);

        Claims claims = jwtUtil.extractClaims(token);

        String jti = claims.getId();

        Instant expiry = claims.getExpiration().toInstant();

        jwtBlacklistService.blacklist(jti, expiry);
    }
}
