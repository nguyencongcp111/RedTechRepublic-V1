package com.example.auth_service.controller;
import com.example.auth_service.dto.request.*;
import com.example.auth_service.dto.response.*;
import com.example.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    public record RegisteredUser(UUID id, String username, String role) {}
    @PostMapping("/register")
    public ResponseEntity<DataResponse<RegisteredUser>> register(@Valid @RequestBody RegisterRequest request) {
        var user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse<>(HttpStatus.CREATED,
                new RegisteredUser(user.getId(), user.getUsername(), user.getRole()), "Đăng ký thành công"));
    }
    @PostMapping("/login")
    public ResponseEntity<DataResponse<JWTResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(new DataResponse<>(HttpStatus.OK, userService.loginUser(request), "Đăng nhập thành công"));
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<DataResponse<JWTResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(new DataResponse<>(HttpStatus.OK, userService.refreshAccessToken(request.getRefreshToken()), "Đã làm mới access token"));
    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authorization) {
        userService.logout(authorization);
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }
}
