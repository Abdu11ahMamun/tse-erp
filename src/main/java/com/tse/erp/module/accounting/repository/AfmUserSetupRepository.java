package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmUserSetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AfmUserSetupRepository extends JpaRepository<AfmUserSetup, Long> {

    List<AfmUserSetup> findByBuIdOrderByIdDesc(Long buId);

    List<AfmUserSetup> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    Optional<AfmUserSetup> findByBuIdAndUserId(Long buId, Long userId);

    boolean existsByBuIdAndUserId(Long buId, Long userId);

    @Query("SELECT s FROM AfmUserSetup s WHERE " +
           "(:bgId IS NULL OR s.bgId = :bgId) AND " +
           "(:buId IS NULL OR s.buId = :buId) AND " +
           "(:userId IS NULL OR s.userId = :userId) AND " +
           "(:status IS NULL OR s.status = :status) " +
           "ORDER BY s.id DESC")
    List<AfmUserSetup> searchUserSetups(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("userId") Long userId,
            @Param("status") Integer status);
}

