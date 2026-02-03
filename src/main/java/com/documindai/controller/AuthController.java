package com.documindai.controller;

import com.documindai.dto.request.LoginRequest;
import com.documindai.dto.request.RegisterRequest;
import com.documindai.dto.response.ApiResponse;
import com.documindai.dto.response.AuthResponse;
import com.documindai.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho authentication endpoints
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    /**
     * Register endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for username: {}", request.getUsername());

        AuthResponse response = authService.register(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("OK", "Service is running"));
    }
}
