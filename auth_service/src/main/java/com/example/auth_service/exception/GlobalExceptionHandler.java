package com.example.auth_service.exception;

import com.example.auth_service.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleWrongUsernameOrPassword(
            BadCredentialsException exception
    ) {

        ErrorResponse response = new ErrorResponse<>();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(HttpStatus.UNAUTHORIZED);
        response.setMessage(exception.getMessage());

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleInvalidRefreshToken(
            NoSuchElementException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Refresh token không hợp lệ");
    }
}
