package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmploymentStatusRepository
        extends JpaRepository<EmploymentStatus, Long> {

    List<EmploymentStatus> findAllByOrderBySortOrderAscIdAsc();

    List<EmploymentStatus> findByBuIdOrderBySortOrderAscIdAsc(Long buId);

    List<EmploymentStatus> findByBuIdAndStatusNameIgnoreCase(
            Long buId, String statusName);

    List<EmploymentStatus> findByBuIdAndShortNameIgnoreCase(
            Long buId, String shortName);
}