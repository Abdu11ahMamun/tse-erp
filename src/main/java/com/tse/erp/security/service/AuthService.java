package com.tse.erp.security.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.entity.Role;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.admin.repository.RoleRepository;
import com.tse.erp.security.LoginAuditLogRepository;
import com.tse.erp.security.jwt.JwtUtil;
import com.tse.erp.security.model.LoginAuditLog;
import com.tse.erp.security.model.LoginRequest;
import com.tse.erp.security.model.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LoginAuditLogRepository auditLogRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.rate-limit.max-attempts-per-account}")
    private int maxAttemptsPerAccount;

    @Value("${security.rate-limit.window-seconds}")
    private int windowSeconds;

    // =========================================
    // LOGIN
    // =========================================
    public TokenResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String deviceInfo = httpRequest.getHeader("User-Agent");
        String username = request.getUsername().trim();

        // ── Rate limit check (per account) ──
        checkAccountRateLimit(username);

        // ── User exist check ──
        User user = userRepository
                .findByUserName(username)
                .orElseGet(() -> {
                    // User nai — log kore error throw koro
                    saveAuditLog(
                            username, null, ipAddress,
                            deviceInfo,
                            LoginAuditLog.AuthStatus.FAILURE,
                            "User not found");
                    throw new BadRequestException(
                            "Invalid username or password");
                });

        // ── Active check ──
        if (user.getIsActive() != 1) {
            saveAuditLog(
                    username, user.getId(), ipAddress,
                    deviceInfo,
                    LoginAuditLog.AuthStatus.FAILURE,
                    "Account is inactive");
            throw new BadRequestException(
                    "Account is inactive. Contact administrator.");
        }

        // ── Password check ──
        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            saveAuditLog(
                    username, user.getId(), ipAddress,
                    deviceInfo,
                    LoginAuditLog.AuthStatus.FAILURE,
                    "Invalid password");
            throw new BadRequestException(
                    "Invalid username or password");
        }

        // ── Get role name ──
        String roleName = "USER";
        if (user.getRoleId() != null) {
            roleName = roleRepository
                    .findById(user.getRoleId())
                    .map(Role::getRoleName)
                    .orElse("USER");
        }

        // ── Generate tokens ──
        String accessToken = jwtUtil.generateAccessToken(
                username, user.getId(), roleName);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        // ── Success log ──
        saveAuditLog(
                username, user.getId(), ipAddress,
                deviceInfo,
                LoginAuditLog.AuthStatus.SUCCESS,
                null);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .username(username)
                .role(roleName)
                .build();
    }

    // =========================================
    // REFRESH TOKEN
    // =========================================
    public TokenResponse refreshToken(String refreshToken) {

        // Validate refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BadRequestException(
                    "Provided token is not a refresh token");
        }

        String username = jwtUtil.extractUsername(refreshToken);

        User user = userRepository
                .findByUserName(username)
                .orElseThrow(() -> new BadRequestException(
                        "User not found"));

        if (user.getIsActive() != 1) {
            throw new BadRequestException("Account is inactive");
        }

        String roleName = "USER";
        if (user.getRoleId() != null) {
            roleName = roleRepository
                    .findById(user.getRoleId())
                    .map(Role::getRoleName)
                    .orElse("USER");
        }

        // New access token generate koro
        String newAccessToken = jwtUtil.generateAccessToken(
                username, user.getId(), roleName);

        // New refresh token generate koro (rotation)
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .username(username)
                .role(roleName)
                .build();
    }

    // =========================================
    // RATE LIMIT CHECK
    // =========================================
    private void checkAccountRateLimit(String username) {
        LocalDateTime windowStart = LocalDateTime.now()
                .minusSeconds(windowSeconds);

        long failedAttempts = auditLogRepository
                .countByUsernameAndAuthStatusAndCreatedAtAfter(
                        username,
                        LoginAuditLog.AuthStatus.FAILURE,
                        windowStart);

        if (failedAttempts >= maxAttemptsPerAccount) {
            throw new BadRequestException(
                    "Too many failed attempts. " +
                            "Please try again after " +
                            windowSeconds + " seconds.");
        }
    }

    // =========================================
    // SAVE AUDIT LOG
    // =========================================
    private void saveAuditLog(
            String username,
            Long userId,
            String ipAddress,
            String deviceInfo,
            LoginAuditLog.AuthStatus status,
            String failureReason) {

        LoginAuditLog log = LoginAuditLog.builder()
                .username(username)
                .userId(userId)
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .authStatus(status)
                .failureReason(failureReason)
                .build();

        auditLogRepository.save(log);
    }

    // =========================================
    // GET CLIENT IP
    // =========================================
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        // Multiple IPs thakle first one nao
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}