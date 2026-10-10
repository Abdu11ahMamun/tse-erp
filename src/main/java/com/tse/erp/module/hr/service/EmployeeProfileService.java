package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.EmployeeCertification;
import com.tse.erp.module.hr.entity.EmployeeEducation;
import com.tse.erp.module.hr.entity.EmployeeJobExperience;
import com.tse.erp.module.hr.entity.EmployeeTraining;

import java.util.List;

public interface EmployeeProfileService {

    List<EmployeeEducation> getEducations(Long employeeId);

    List<EmployeeEducation> replaceEducations(
            Long employeeId, List<EmployeeEducation> rows);

    List<EmployeeTraining> getTrainings(Long employeeId);

    List<EmployeeTraining> replaceTrainings(
            Long employeeId, List<EmployeeTraining> rows);

    List<EmployeeCertification> getCertifications(Long employeeId);

    List<EmployeeCertification> replaceCertifications(
            Long employeeId, List<EmployeeCertification> rows);

    List<EmployeeJobExperience> getJobExperiences(Long employeeId);

    List<EmployeeJobExperience> replaceJobExperiences(
            Long employeeId, List<EmployeeJobExperience> rows);
}
