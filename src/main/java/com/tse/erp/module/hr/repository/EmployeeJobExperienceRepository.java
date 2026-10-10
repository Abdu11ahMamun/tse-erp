package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmployeeJobExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeJobExperienceRepository
        extends JpaRepository<EmployeeJobExperience, Long> {

    List<EmployeeJobExperience> findByEmployeeIdAndStatusOrderByIdAsc(
            Long employeeId, Integer status);

    List<EmployeeJobExperience> findByEmployeeIdOrderByIdAsc(Long employeeId);
}
