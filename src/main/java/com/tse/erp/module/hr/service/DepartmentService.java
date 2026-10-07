package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> getAllDepartments(Long bgId);

    Department getDepartmentById(Long id);

    Department createDepartment(Department department);

    Department updateDepartment(Long id, Department department);

    void deleteDepartment(Long id);
}
