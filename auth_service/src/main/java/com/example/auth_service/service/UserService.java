package com.example.auth_service.service;

import com.example.auth_service.dto.request.LoginRequest;
import com.example.auth_service.dto.request.RegisterRequest;
import com.example.auth_service.dto.response.JWTResponse;
import com.example.auth_service.entity.User;

public interface UserService {
    User registerUser(RegisterRequest registerRequest);

    JWTResponse loginUser(LoginRequest loginRequest);

    JWTResponse refreshAccessToken(String token);

    void logout(String token);
}
