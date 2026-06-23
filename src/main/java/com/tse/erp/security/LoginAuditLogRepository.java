package com.tse.erp.security;

import com.tse.erp.security.model.LoginAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAuditLogRepository
        extends JpaRepository<LoginAuditLog, Long> {

    // Username diye logs
    List<LoginAuditLog> findByUsernameOrderByCreatedAtDesc(
            String username);

    // IP diye recent failures
    long countByIpAddressAndAuthStatusAndCreatedAtAfter(
            String ipAddress,
            LoginAuditLog.AuthStatus authStatus,
            LocalDateTime after);

    // Account diye recent failures
    long countByUsernameAndAuthStatusAndCreatedAtAfter(
            String username,
            LoginAuditLog.AuthStatus authStatus,
            LocalDateTime after);
}