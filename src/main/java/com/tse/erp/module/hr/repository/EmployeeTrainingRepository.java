package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmployeeTraining;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeTrainingRepository
        extends JpaRepository<EmployeeTraining, Long> {

    List<EmployeeTraining> findByEmployeeIdAndStatusOrderByIdAsc(
            Long employeeId, Integer status);

    List<EmployeeTraining> findByEmployeeIdOrderByIdAsc(Long employeeId);
}
