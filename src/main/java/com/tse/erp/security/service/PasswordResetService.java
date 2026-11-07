package com.tse.erp.security.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.entity.UserEmplMap;
import com.tse.erp.module.admin.repository.UserEmplMapRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.security.PasswordResetOtpRepository;
import com.tse.erp.security.model.PasswordResetOtp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserEmplMapRepository userEmplMapRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${otp.expiry-minutes:5}")
    private int otpExpiryMinutes;
    @Value("${otp.max-attempts:5}")
    private int maxAttempts;
    @Value("${otp.resend-cooldown-seconds:60}")
    private int cooldownSeconds;
    @Value("${otp.max-per-user-per-hour:3}")
    private int maxPerUser;
    @Value("${otp.max-per-ip-per-hour:10}")
    private int maxPerIp;
    @Value("${reset-token.expiry-minutes:10}")
    private int tokenExpiryMinutes;
    @Value("${app.mail.from:}")
    private String mailFrom;

    // =========================================
    // 1. FORGOT PASSWORD
    // =========================================
    @Transactional
    public void requestOtp(String identifier, String ip) {

        if (identifier == null || identifier.isBlank()) {
            throw new BadRequestException("Username or email is required");
        }
        String id = identifier.trim();
        LocalDateTime now = LocalDateTime.now();

        // IP-wise limit (user thakuk na thakuk)
        if (otpRepository.countByRequestIpAndCreatedDatetimeAfter(
                ip, now.minusHours(1)) >= maxPerIp) {
            throw new TooManyRequests();
        }
        logIpHit(ip, now);

        Optional<User> userOpt = findUser(id);
        if (userOpt.isEmpty()
                || !Integer.valueOf(1).equals(userOpt.get().getStatus())) {
            return; // generic response, kichu bola jabe na
        }
        User user = userOpt.get();

        String email = resolveEmail(user);
        if (email == null) {
            return;
        }

        // Per-user cooldown + hourly limit
        Optional<PasswordResetOtp> last =
                otpRepository.findFirstByUserIdOrderByIdDesc(user.getId());
        if (last.isPresent() && last.get().getOtpHash() != null
                && last.get().getCreatedDatetime()
                .isAfter(now.minusSeconds(cooldownSeconds))) {
            throw new TooManyRequests();
        }
        if (otpRepository.countByUserIdAndCreatedDatetimeAfter(
                user.getId(), now.minusHours(1)) >= maxPerUser) {
            throw new TooManyRequests();
        }

        // Purono unused OTP invalid
        for (PasswordResetOtp o : otpRepository
                .findByUserIdAndUsedFalse(user.getId())) {
            o.setUsed(true);
            otpRepository.save(o);
        }

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        String salt = randomHex(16);

        PasswordResetOtp rec = new PasswordResetOtp();
        rec.setUserId(user.getId());
        rec.setOtpSalt(salt);
        rec.setOtpHash(sha256(salt + otp));
        rec.setExpiresAt(now.plusMinutes(otpExpiryMinutes));
        rec.setRequestIp(ip);
        rec.setCreatedDatetime(now);
        otpRepository.save(rec);

        sendEmail(email,
                "Your password reset code",
                "Your OTP is " + otp + ". It expires in "
                        + otpExpiryMinutes + " minutes.\n"
                        + "If you did not request this, ignore this email.");
    }

    // =========================================
    // 2. VERIFY OTP
    // =========================================
    @Transactional(noRollbackFor = BadRequestException.class)
    public String verifyOtp(String identifier, String otp) {

        if (identifier == null || otp == null || !otp.matches("\\d{6}")) {
            throw invalid();
        }
        Optional<User> userOpt = findUser(identifier.trim());
        if (userOpt.isEmpty()) {
            throw invalid();
        }
        Long userId = userOpt.get().getId();

        PasswordResetOtp rec = otpRepository
                .findFirstByUserIdAndUsedFalseOrderByIdDesc(userId)
                .orElseThrow(this::invalid);

        if (rec.getOtpHash() == null
                || rec.getExpiresAt().isBefore(LocalDateTime.now())
                || rec.getAttempts() >= maxAttempts) {
            throw invalid();
        }

        rec.setAttempts(rec.getAttempts() + 1);

        boolean match = MessageDigest.isEqual(
                sha256(rec.getOtpSalt() + otp).getBytes(StandardCharsets.UTF_8),
                rec.getOtpHash().getBytes(StandardCharsets.UTF_8));
        if (!match) {
            if (rec.getAttempts() >= maxAttempts) {
                rec.setUsed(true);
            }
            otpRepository.save(rec);
            throw invalid();
        }

        String token = randomToken();
        rec.setResetTokenHash(sha256(token));
        rec.setResetTokenExpiresAt(
                LocalDateTime.now().plusMinutes(tokenExpiryMinutes));
        rec.setUsed(true); // OTP ekbar-i
        otpRepository.save(rec);
        return token;
    }

    // =========================================
    // 3. RESET PASSWORD
    // =========================================
    @Transactional
    public void resetPassword(String resetToken, String newPassword,
                              String confirmPassword) {

        if (resetToken == null || resetToken.isBlank()) {
            throw new BadRequestException("Invalid or expired reset token");
        }
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            throw new BadRequestException(
                    "Password and Confirm Password do not match");
        }
        if (!newPassword.matches(PASSWORD_REGEX)) {
            throw new BadRequestException(
                    "Password must be 8-64 characters and contain an uppercase letter, "
                            + "a lowercase letter, a number and a special character");
        }

        PasswordResetOtp rec = otpRepository
                .findByResetTokenHash(sha256(resetToken))
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired reset token"));

        if (rec.getResetTokenExpiresAt() == null
                || rec.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = userRepository.findById(rec.getUserId())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired reset token"));
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Token ar baki OTP invalid
        rec.setResetTokenHash(null);
        rec.setResetTokenExpiresAt(null);
        otpRepository.save(rec);
        for (PasswordResetOtp o : otpRepository
                .findByUserIdAndUsedFalse(user.getId())) {
            o.setUsed(true);
            otpRepository.save(o);
        }

        String email = resolveEmail(user);
        if (email != null) {
            sendEmail(email, "Your password was changed",
                    "Your password was just changed. "
                            + "If this was not you, contact your administrator immediately.");
        }
    }

    // =========================================
    // HELPERS
    // =========================================
    private Optional<User> findUser(String id) {
        Optional<User> byName = userRepository.findByUserName(id);
        if (byName.isPresent()) {
            return byName;
        }
        return userRepository.findByEmailIgnoreCase(id).stream().findFirst();
    }

    // User.email, na thakle linked Employee officeEmail
    private String resolveEmail(User user) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        return userEmplMapRepository.findByUserIdOrderByIdAsc(user.getId())
                .stream()
                .filter(m -> Integer.valueOf(1).equals(m.getStatus()))
                .map(UserEmplMap::getEmplId)
                .findFirst()
                .flatMap(employeeRepository::findById)
                .map(e -> e.getOfficeEmail())
                .filter(s -> s != null && !s.isBlank())
                .orElse(null);
    }

    // IP-wise gonar jonno otp-hash chhara row
    private void logIpHit(String ip, LocalDateTime now) {
        PasswordResetOtp hit = new PasswordResetOtp();
        hit.setRequestIp(ip);
        hit.setUsed(true);
        hit.setCreatedDatetime(now);
        otpRepository.save(hit);
    }

    @Async
    protected void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(mailFrom);
            m.setTo(to);
            m.setSubject(subject);
            m.setText(text);
            mailSender.send(m);
        } catch (Exception e) {
            // OTP/email content log kora jabe na
            log.warn("Password reset email failed: {}", e.getClass().getSimpleName());
        }
    }

    private BadRequestException invalid() {
        return new BadRequestException("Invalid or expired OTP");
    }

    private String randomHex(int bytes) {
        byte[] b = new byte[bytes];
        RANDOM.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    private String randomToken() {
        byte[] b = new byte[32];
        RANDOM.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private String sha256(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static class TooManyRequests extends RuntimeException {
        public TooManyRequests() {
            super("Too many requests. Please try again later.");
        }
    }
}