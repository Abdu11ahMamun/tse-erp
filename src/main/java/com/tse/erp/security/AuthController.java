package com.tse.erp.security;

import com.tse.erp.security.model.*;
import com.tse.erp.security.service.AuthService;
import com.tse.erp.security.service.SelfRegisterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final SelfRegisterService selfRegisterService;

    // =========================================
    // LOGIN
    // =========================================
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        TokenResponse response = authService.login(
                request, httpRequest);
        return ResponseEntity.ok(response);
    }

    // =========================================
    // REFRESH TOKEN
    // =========================================
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        TokenResponse response = authService
                .refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // =========================================
    // LOGOUT (client side token delete)
    // =========================================
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok(
                "Logged out successfully");
    }

    @PostMapping("/register")
    public ResponseEntity<SelfRegisterResponse> register(
            @RequestBody SelfRegisterRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(selfRegisterService.register(request, httpRequest));
    }
}