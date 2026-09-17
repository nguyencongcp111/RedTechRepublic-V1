package com.example.auth_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public void blacklist(String jti, Instant expiry) {
        long ttl = Duration.between(
                Instant.now(),
                expiry
        ).getSeconds();

        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                    "blacklist:" + jti,
                    "true",
                    ttl,
                    TimeUnit.SECONDS
            );
        }
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("blacklist:" + jti)
        );
    }
}
