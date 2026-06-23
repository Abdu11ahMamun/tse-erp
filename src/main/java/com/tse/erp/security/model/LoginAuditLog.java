package com.tse.erp.security.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "auth_status")
    @Enumerated(EnumType.STRING)
    private AuthStatus authStatus;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // =========================================
    // Enum
    // =========================================
    public enum AuthStatus {
        SUCCESS, FAILURE
    }

    // =========================================
    // Auto timestamp
    // =========================================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}