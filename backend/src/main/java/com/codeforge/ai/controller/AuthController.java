package com.codeforge.ai.controller;

import com.codeforge.ai.dto.ApiResponse;
import com.codeforge.ai.dto.AuthResponse;
import com.codeforge.ai.dto.LoginRequest;
import com.codeforge.ai.dto.SignupRequest;
import com.codeforge.ai.service.AuthService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
// Allow local dev Vite servers (4173/4174) and common ports used during development
public class AuthController {

    private AuthService authService;

    @PostConstruct
    public void startupCheck() {
        System.out.println("==================================================");
        System.out.println("CODEFORGE_BUILD_JUNE3");
        System.out.println("==================================================");
        log.info("CODEFORGE_BUILD_JUNE3 - Backend initialized");
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        log.debug("Received signup request: {}", signupRequest);
        AuthResponse authResponse = authService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "User registered successfully", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Login successful", authResponse));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Auth service is running", "OK"));
    }
}