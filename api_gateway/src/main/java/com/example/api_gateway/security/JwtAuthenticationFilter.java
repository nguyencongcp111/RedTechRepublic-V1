package com.example.api_gateway.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final SecretKey key;
    private final StringRedisTemplate redis;
    public JwtAuthenticationFilter(@Value("${JWT_SECRET}") String secret, StringRedisTemplate redis) {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.redis = redis;
    }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return Set.of("/api/auth/login", "/api/auth/register", "/api/auth/refresh-token", "/actuator/health", "/error").contains(request.getRequestURI());
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) { chain.doFilter(request, response); return; }
        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
            UUID.fromString(Objects.toString(claims.get("userId", String.class), ""));
            if (claims.getSubject() == null || claims.getId() == null || claims.getExpiration() == null
                    || !Set.of("USER", "ADMIN").contains(Objects.toString(claims.get("role", String.class), ""))) throw new IllegalArgumentException("Invalid claims");
        } catch (JwtException | IllegalArgumentException ex) { reject(response, 401, "Token không hợp lệ hoặc đã hết hạn"); return; }
        try {
            if (Boolean.TRUE.equals(redis.hasKey("blacklist:" + claims.getId()))) { reject(response, 401, "Phiên đăng nhập đã kết thúc"); return; }
        } catch (org.springframework.dao.DataAccessException ex) { reject(response, 503, "Dịch vụ xác thực tạm thời không khả dụng"); return; }
        var authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
    private void reject(HttpServletResponse response, int status, String detail) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(status); response.setContentType("application/problem+json"); response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"detail\":\"" + detail + "\"}");
    }
}
