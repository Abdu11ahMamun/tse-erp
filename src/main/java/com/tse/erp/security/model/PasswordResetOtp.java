package com.tse.erp.security.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_password_reset_otp", indexes = {
        @Index(name = "idx_pro_user", columnList = "user_id"),
        @Index(name = "idx_pro_token", columnList = "reset_token_hash"),
        @Index(name = "idx_pro_ip", columnList = "request_ip")})
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    // user nai hole null (rate-limit gonar jonno IP-wise row)
    @Column(name = "otp_salt", length = 32)
    private String otpSalt;

    @Column(name = "otp_hash", length = 64)
    private String otpHash;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "used", nullable = false)
    private Boolean used = false;

    @Column(name = "reset_token_hash", length = 64)
    private String resetTokenHash;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @Column(name = "request_ip", length = 64)
    private String requestIp;

    @Column(name = "created_datetime", nullable = false)
    private LocalDateTime createdDatetime;
}