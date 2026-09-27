package com.example.order_service.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(ResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler({InvalidOrderStateException.class, OrderItemAlreadyExistsException.class,
            IdempotencyKeyConflictException.class, DataIntegrityViolationException.class,
            ObjectOptimisticLockingFailureException.class})
    ResponseEntity<ProblemDetail> conflict(RuntimeException exception) {
        return problem(HttpStatus.CONFLICT, "ORDER_CONFLICT", exception.getMessage());
    }

    @ExceptionHandler(DownstreamServiceException.class)
    ResponseEntity<ProblemDetail> downstream(DownstreamServiceException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "PRODUCT_SERVICE_UNAVAILABLE", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Dữ liệu đầu vào không hợp lệ");
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> unreadable(HttpMessageNotReadableException exception) {
        String msg = exception.getMessage();
        String detail = (msg != null && msg.length() > 200) ? msg.substring(0, 200) : msg;
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY", detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> constraintViolation(ConstraintViolationException exception) {
        String detail = exception.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse("Dữ liệu không hợp lệ");
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", detail);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<ProblemDetail> missingHeader(MissingRequestHeaderException exception) {
        return problem(HttpStatus.BAD_REQUEST, "MISSING_HEADER",
                "Header bắt buộc bị thiếu: " + exception.getHeaderName());
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status,
                detail == null ? status.getReasonPhrase() : detail);
        problem.setTitle(code);
        problem.setType(URI.create("https://ecommerce.local/problems/" + code.toLowerCase()));
        problem.setProperty("code", code);
        return ResponseEntity.status(status).body(problem);
    }
}
