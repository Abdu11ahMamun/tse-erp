package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmployeeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeEducationRepository
        extends JpaRepository<EmployeeEducation, Long> {

    List<EmployeeEducation> findByEmployeeIdAndStatusOrderByIdAsc(
            Long employeeId, Integer status);

    List<EmployeeEducation> findByEmployeeIdOrderByIdAsc(Long employeeId);
}
