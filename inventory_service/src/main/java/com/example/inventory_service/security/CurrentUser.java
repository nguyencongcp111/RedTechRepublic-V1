package com.example.inventory_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Reads the caller identity from the Bearer token forwarded by the API gateway.
 * The signature is verified again here so a request that reaches this service
 * directly cannot forge a poster or a role.
 */
@Component
public class CurrentUser {
    public static final Set<String> KNOWN_ROLES = Set.of("USER", "SELLER", "ADMIN");

    private final String secret;
    private volatile SecretKey key;

    public CurrentUser(@Value("${JWT_SECRET:}") String secret) {
        this.secret = secret;
    }

    public record Principal(String username, String role) {
        public boolean isAdmin() {
            return "ADMIN".equals(role);
        }
    }

    public Principal require(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cần đăng nhập");
        }
        try {
            Claims claims = Jwts.parser().verifyWith(key()).build()
                    .parseSignedClaims(header.substring(7)).getPayload();
            String role = claims.get("role", String.class);
            if (claims.getSubject() == null || !KNOWN_ROLES.contains(role)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token không hợp lệ");
            }
            return new Principal(claims.getSubject(), role);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn");
        }
    }

    /** ADMIN hoặc SELLER. */
    public Principal requirePublisher(HttpServletRequest request) {
        Principal principal = require(request);
        if (!principal.isAdmin() && !"SELLER".equals(principal.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ ADMIN hoặc SELLER được thực hiện thao tác này");
        }
        return principal;
    }

    private SecretKey key() {
        if (key == null) {
            if (secret == null || secret.isBlank()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình JWT_SECRET");
            }
            key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        return key;
    }
}
