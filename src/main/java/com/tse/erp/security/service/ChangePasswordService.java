package com.tse.erp.security.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.repository.RoleRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.security.AuthUser;
import com.tse.erp.security.CurrentUser;
import com.tse.erp.security.LoginAuditLogRepository;
import com.tse.erp.security.jwt.JwtUtil;
import com.tse.erp.security.model.ChangePasswordRequest;
import com.tse.erp.security.model.LoginAuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAuditLogRepository auditLogRepository;
    private final JwtUtil jwtUtil;

    @Value("${security.rate-limit.max-attempts-per-account}")
    private int maxAttempts;
    @Value("${security.rate-limit.window-seconds}")
    private int windowSeconds;
    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Transactional(noRollbackFor = BadRequestException.class)
    public Map<String, Object> change(ChangePasswordRequest req, String ip) {

        AuthUser me = CurrentUser.get().orElseThrow(
                () -> new UnauthorizedException("Authentication required"));

        User user = userRepository.findById(me.userId()).orElseThrow(
                () -> new UnauthorizedException("Authentication required"));

        if (req.getCurrentPassword() == null
                || req.getNewPassword() == null
                || req.getConfirmPassword() == null) {
            throw new BadRequestException("All password fields are required");
        }

        // Vul current password bar bar korle block (login-er limit-er shathe ek-i counter)
        LocalDateTime windowStart =
                LocalDateTime.now().minusSeconds(windowSeconds);
        long fails = auditLogRepository
                .countByUsernameAndAuthStatusAndCreatedAtAfter(
                        user.getUserName(),
                        LoginAuditLog.AuthStatus.FAILURE, windowStart);
        if (fails >= maxAttempts) {
            throw new BadRequestException(
                    "Too many failed attempts. Please try again later.");
        }

        if (!passwordEncoder.matches(
                req.getCurrentPassword(), user.getPassword())) {
            auditLogRepository.save(LoginAuditLog.builder()
                    .username(user.getUserName())
                    .userId(user.getId())
                    .ipAddress(ip)
                    .authStatus(LoginAuditLog.AuthStatus.FAILURE)
                    .failureReason("Change password: wrong current password")
                    .build());
            throw new BadRequestException("Current password is incorrect");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException(
                    "Password and Confirm Password do not match");
        }
        if (!req.getNewPassword().matches(PASSWORD_REGEX)) {
            throw new BadRequestException(
                    "Password must be 8-64 characters and contain an uppercase letter, "
                            + "a lowercase letter, a number and a special character");
        }
        if (passwordEncoder.matches(
                req.getNewPassword(), user.getPassword())) {
            throw new BadRequestException(
                    "New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        auditLogRepository.save(LoginAuditLog.builder()
                .username(user.getUserName())
                .userId(user.getId())
                .ipAddress(ip)
                .authStatus(LoginAuditLog.AuthStatus.SUCCESS)
                .failureReason("Password changed")
                .build());

        // Purono token ekhon invalid, tai notun token dei
        String roleName = "USER";
        if (user.getRoleId() != null) {
            roleName = roleRepository.findById(user.getRoleId())
                    .map(r -> r.getRoleName()).orElse("USER");
        }
        String token = jwtUtil.generateAccessToken(
                user.getUserName(), user.getId(), roleName,
                me.empId(), user.getBgId(), user.getBuId());

        return Map.of(
                "message", "Password changed successfully",
                "accessToken", token,
                "tokenType", "Bearer",
                "expiresIn", (int) (accessTokenExpiry / 1000));
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String m) {
            super(m);
        }
    }
}