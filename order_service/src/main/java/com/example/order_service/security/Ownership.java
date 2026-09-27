package com.example.order_service.security;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.AccessDeniedException;
import java.util.UUID;
public final class Ownership {
    private Ownership() {}
    public static void require(UUID ownerId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) throw new AccessDeniedException("Cần đăng nhập");
        if ("ADMIN".equals(jwt.getClaimAsString("role"))) return;
        if (!ownerId.toString().equals(jwt.getClaimAsString("userId"))) throw new AccessDeniedException("Bạn không có quyền truy cập dữ liệu này");
    }
    public static boolean service() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof Jwt jwt && "SERVICE".equals(jwt.getClaimAsString("role"));
    }
}
