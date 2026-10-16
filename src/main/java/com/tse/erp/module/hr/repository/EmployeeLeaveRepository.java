package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmployeeLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLeaveRepository
        extends JpaRepository<EmployeeLeave, Long> {

    List<EmployeeLeave> findAllByOrderByIdDesc();

    List<EmployeeLeave> findByEmployeeIdOrderByIdDesc(Long employeeId);

    List<EmployeeLeave> findByStatusOrderByIdDesc(Integer status);

    List<EmployeeLeave> findByEmployeeIdAndStatus(
            Long employeeId, Integer status);

    List<EmployeeLeave> findByEmployeeIdAndLeaveCategoryIdAndStatus(
            Long employeeId, Long leaveCategoryId, Integer status);
}
