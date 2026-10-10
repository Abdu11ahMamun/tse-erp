package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmployeeCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeCertificationRepository
        extends JpaRepository<EmployeeCertification, Long> {

    List<EmployeeCertification> findByEmployeeIdAndStatusOrderByIdAsc(
            Long employeeId, Integer status);

    List<EmployeeCertification> findByEmployeeIdOrderByIdAsc(Long employeeId);
}
