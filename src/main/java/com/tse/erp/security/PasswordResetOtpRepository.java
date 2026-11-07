package com.tse.erp.security;

import com.tse.erp.security.model.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findFirstByUserIdAndUsedFalseOrderByIdDesc(Long userId);

    Optional<PasswordResetOtp> findFirstByUserIdOrderByIdDesc(Long userId);

    long countByUserIdAndCreatedDatetimeAfter(Long userId, LocalDateTime t);

    long countByRequestIpAndCreatedDatetimeAfter(String ip, LocalDateTime t);

    List<PasswordResetOtp> findByUserIdAndUsedFalse(Long userId);

    Optional<PasswordResetOtp> findByResetTokenHash(String hash);
}