package com.example.auth_service.controller;

import com.example.auth_service.entity.InfoSeller;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.SellerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class SellerController {
    private final SellerService sellers;
    private final UserRepository users;

    // ── Người dùng ────────────────────────────────────────────────
    @PostMapping("/seller-applications")
    public ResponseEntity<SellerService.SellerInfo> apply(Authentication auth,
                                                          @Valid @RequestBody SellerService.ApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sellers.apply(currentUserId(auth), request));
    }

    /** 204 nếu user chưa từng ứng tuyển. */
    @GetMapping("/seller-applications/me")
    public ResponseEntity<SellerService.SellerInfo> mine(Authentication auth) {
        return sellers.mine(currentUserId(auth)).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    // ── Quản trị (ADMIN, xem SecurityConfig) ──────────────────────
    @GetMapping("/seller-applications")
    public Map<String, Object> applications(@RequestParam(required = false) InfoSeller.Status status,
                                            @RequestParam(defaultValue = "0") @Min(0) int page,
                                            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return sellers.applications(status, page, size);
    }

    @PostMapping("/seller-applications/{id}/approve")
    public SellerService.SellerInfo approve(@PathVariable UUID id, Authentication auth) {
        return sellers.approve(id, auth.getName());
    }

    @PostMapping("/seller-applications/{id}/reject")
    public SellerService.SellerInfo reject(@PathVariable UUID id, Authentication auth,
                                           @Valid @RequestBody(required = false) SellerService.RejectRequest request) {
        return sellers.reject(id, auth.getName(), request == null ? null : request.reason());
    }

    @GetMapping("/users")
    public Map<String, Object> listUsers(@RequestParam(required = false) String role,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(defaultValue = "0") @Min(0) int page,
                                         @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return sellers.listUsers(role, keyword, page, size);
    }

    private UUID currentUserId(Authentication auth) {
        return users.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản không tồn tại"))
                .getId();
    }
}
