package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    List<Department> findAllByOrderByIdDesc();

    List<Department> findByBgIdOrderByIdDesc(Long bgId);

    List<Department> findByBgIdAndDepartmentNameIgnoreCase(
            Long bgId, String departmentName);

    List<Department> findByBgIdAndShortNameIgnoreCase(
            Long bgId, String shortName);
}
