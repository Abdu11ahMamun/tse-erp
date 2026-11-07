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
import com.tse.erp.security.service.PasswordResetService;
import com.tse.erp.security.service.ChangePasswordService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final SelfRegisterService selfRegisterService;
    private final PasswordResetService passwordResetService;
    private final ChangePasswordService changePasswordService;

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

    @PostMapping("/forgot-password")
    public ResponseEntity<java.util.Map<String, String>> forgotPassword(
            @RequestBody ForgotPasswordRequest req,
            HttpServletRequest http) {
        passwordResetService.requestOtp(req.getIdentifier(), clientIp(http));
        return ResponseEntity.ok(java.util.Map.of("message",
                "If an account exists, an OTP has been sent to its email"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<java.util.Map<String, String>> verifyOtp(
            @RequestBody VerifyOtpRequest req) {
        String token = passwordResetService
                .verifyOtp(req.getIdentifier(), req.getOtp());
        return ResponseEntity.ok(java.util.Map.of("resetToken", token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<java.util.Map<String, String>> resetPassword(
            @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.getResetToken(),
                req.getNewPassword(), req.getConfirmPassword());
        return ResponseEntity.ok(java.util.Map.of("message",
                "Password has been reset. Please log in."));
    }

    private String clientIp(HttpServletRequest r) {
        String ip = r.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = r.getRemoteAddr();
        }
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }

    @PostMapping("/change-password")
    public ResponseEntity<java.util.Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequest req,
            HttpServletRequest http) {
        return ResponseEntity.ok(
                changePasswordService.change(req, clientIp(http)));
    }
}