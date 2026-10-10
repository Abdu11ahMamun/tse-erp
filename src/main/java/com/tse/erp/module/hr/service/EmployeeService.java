package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.Employee;

import java.util.List;

public interface EmployeeService {

    List<Employee> getAllEmployees(Long buId);

    Employee getEmployeeById(Long id);

    Employee createEmployee(Employee employee);

    Employee updateEmployee(Long id, Employee employee);
}
