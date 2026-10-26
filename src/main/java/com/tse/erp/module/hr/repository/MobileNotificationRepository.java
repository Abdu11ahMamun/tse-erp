package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.MobileNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MobileNotificationRepository
        extends JpaRepository<MobileNotification, Long> {

    List<MobileNotification> findAllByOrderByIdDesc();

    List<MobileNotification> findByBuIdOrderByIdDesc(Long buId);

    List<MobileNotification> findByEmplIdOrderByIdDesc(Long emplId);

    List<MobileNotification> findByBuIdAndEmplIdOrderByIdDesc(
            Long buId, Long emplId);

    boolean existsByEmplId(Long emplId);
}
