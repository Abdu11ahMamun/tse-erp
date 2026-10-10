package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmploymentStatusRepository
        extends JpaRepository<EmploymentStatus, Long> {
}
