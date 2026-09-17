package com.example.auth_service.controller;

import com.example.auth_service.dto.request.LoginRequest;
import com.example.auth_service.dto.request.RefreshTokenRequest;
import com.example.auth_service.dto.request.RegisterRequest;
import com.example.auth_service.dto.response.DataResponse;
import com.example.auth_service.dto.response.JWTResponse;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.jwt.JwtUtil;
import com.example.auth_service.service.RefreshTokenService;
import com.example.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<DataResponse<User>> register (
            @RequestBody RegisterRequest registerRequest) {
        return new ResponseEntity<>(
                new DataResponse<>(
                        HttpStatus.CREATED,
                        userService.registerUser(registerRequest),
                        "Đăng ký thành công"
                ), HttpStatus.CREATED
        );
    }

    @GetMapping("/test-token")
    public ResponseEntity<String> testToken(
            @RequestParam String username
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy user: " + username
                        )
                );

        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<DataResponse<JWTResponse>> login (
            @RequestBody LoginRequest loginRequest) {

        return new ResponseEntity<>(
                new DataResponse<>(
                        HttpStatus.OK,
                        userService.loginUser(loginRequest),
                        "Đăng nhập thành công"
                ), HttpStatus.OK
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<DataResponse<JWTResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {
        JWTResponse response =
                userService.refreshAccessToken(request.getRefreshToken());

        return ResponseEntity.ok(
                new DataResponse<>(
                        HttpStatus.OK,
                        response,
                        "Làm mới access token thành công"
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authorization
    ) {
        userService.logout(authorization);

        return ResponseEntity.ok(
                Map.of("message", "Đăng xuất thành công")
        );
    }

    @GetMapping("/TEST")
    public String test() {
        return "JWT OK";
    }

}
