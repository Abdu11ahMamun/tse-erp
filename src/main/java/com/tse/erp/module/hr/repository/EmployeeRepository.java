package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    List<Employee> findAllByOrderByIdDesc();

    List<Employee> findByBuIdOrderByIdDesc(Long buId);

    List<Employee> findByNid(String nid);

    List<Employee> findByMobileNo(String mobileNo);

    List<Employee> findByEmailIgnoreCase(String email);

    List<Employee> findByPassportNoIgnoreCase(String passportNo);

    List<Employee> findByDrivingLicenseNoIgnoreCase(String drivingLicenseNo);
}
