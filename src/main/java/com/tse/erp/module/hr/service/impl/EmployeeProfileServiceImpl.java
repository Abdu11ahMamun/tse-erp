package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.common.ValidationUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.hr.entity.Degree;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.entity.EmployeeCertification;
import com.tse.erp.module.hr.entity.EmployeeEducation;
import com.tse.erp.module.hr.entity.EmployeeJobExperience;
import com.tse.erp.module.hr.entity.EmployeeTraining;
import com.tse.erp.module.hr.entity.LevelOfEducation;
import com.tse.erp.module.hr.repository.DegreeRepository;
import com.tse.erp.module.hr.repository.EmployeeCertificationRepository;
import com.tse.erp.module.hr.repository.EmployeeEducationRepository;
import com.tse.erp.module.hr.repository.EmployeeJobExperienceRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.hr.repository.EmployeeTrainingRepository;
import com.tse.erp.module.hr.repository.LevelOfEducationRepository;
import com.tse.erp.module.hr.service.EmployeeProfileService;
import com.tse.erp.module.hr.validation.HrLookupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeEducationRepository employeeEducationRepository;
    private final EmployeeTrainingRepository employeeTrainingRepository;
    private final EmployeeCertificationRepository employeeCertificationRepository;
    private final EmployeeJobExperienceRepository employeeJobExperienceRepository;
    private final LevelOfEducationRepository levelOfEducationRepository;
    private final DegreeRepository degreeRepository;
    private final HrLookupValidator hrLookupValidator;

    @Override
    public List<EmployeeEducation> getEducations(Long employeeId) {
        findEmployee(employeeId);
        return employeeEducationRepository
                .findByEmployeeIdAndStatusOrderByIdAsc(employeeId, 1);
    }

    @Override
    @Transactional
    public List<EmployeeEducation> replaceEducations(
            Long employeeId, List<EmployeeEducation> rows) {
        findEmployee(employeeId);
        List<EmployeeEducation> submittedRows =
                rows == null ? List.of() : rows;

        for (EmployeeEducation row : submittedRows) {
            validateEducation(row);
        }

        List<EmployeeEducation> current =
                employeeEducationRepository.findByEmployeeIdOrderByIdAsc(
                        employeeId);
        Map<Long, EmployeeEducation> existingById = new HashMap<>();
        for (EmployeeEducation existing : current) {
            existingById.put(existing.getId(), existing);
        }

        Set<Long> handledIds = new HashSet<>();
        List<EmployeeEducation> newRows = new ArrayList<>();
        for (EmployeeEducation row : submittedRows) {
            if (row.getId() != null) {
                EmployeeEducation existing = existingById.get(row.getId());
                if (existing == null) {
                    throw new BadRequestException(
                            "Education id " + row.getId()
                                    + " does not belong to this Employee");
                }
                copyEducation(row, existing);
                existing.setStatus(
                        row.getStatus() == null ? 1 : row.getStatus());
                handledIds.add(row.getId());
            } else {
                row.setId(null);
                row.setEmployeeId(employeeId);
                if (row.getStatus() != null) {
                    row.setStatus(row.getStatus());
                }
                newRows.add(row);
            }
        }

        for (EmployeeEducation existing : current) {
            if (!handledIds.contains(existing.getId())) {
                existing.setStatus(0);
            }
        }

        employeeEducationRepository.saveAll(current);
        employeeEducationRepository.saveAll(newRows);
        return getEducations(employeeId);
    }

    @Override
    public List<EmployeeTraining> getTrainings(Long employeeId) {
        findEmployee(employeeId);
        return employeeTrainingRepository
                .findByEmployeeIdAndStatusOrderByIdAsc(employeeId, 1);
    }

    @Override
    @Transactional
    public List<EmployeeTraining> replaceTrainings(
            Long employeeId, List<EmployeeTraining> rows) {
        Employee employee = findEmployee(employeeId);
        List<EmployeeTraining> submittedRows =
                rows == null ? List.of() : rows;
        for (EmployeeTraining row : submittedRows) {
            validateTraining(row, employee);
        }

        List<EmployeeTraining> current =
                employeeTrainingRepository.findByEmployeeIdOrderByIdAsc(
                        employeeId);
        Map<Long, EmployeeTraining> existingById = new HashMap<>();
        for (EmployeeTraining existing : current) {
            existingById.put(existing.getId(), existing);
        }

        Set<Long> handledIds = new HashSet<>();
        List<EmployeeTraining> newRows = new ArrayList<>();
        for (EmployeeTraining row : submittedRows) {
            if (row.getId() != null) {
                EmployeeTraining existing = existingById.get(row.getId());
                if (existing == null) {
                    throw new BadRequestException(
                            "Training id " + row.getId()
                                    + " does not belong to this Employee");
                }
                copyTraining(row, existing);
                existing.setStatus(
                        row.getStatus() == null ? 1 : row.getStatus());
                handledIds.add(row.getId());
            } else {
                row.setId(null);
                row.setEmployeeId(employeeId);
                newRows.add(row);
            }
        }
        for (EmployeeTraining existing : current) {
            if (!handledIds.contains(existing.getId())) {
                existing.setStatus(0);
            }
        }

        employeeTrainingRepository.saveAll(current);
        employeeTrainingRepository.saveAll(newRows);
        return getTrainings(employeeId);
    }

    @Override
    public List<EmployeeCertification> getCertifications(Long employeeId) {
        findEmployee(employeeId);
        return employeeCertificationRepository
                .findByEmployeeIdAndStatusOrderByIdAsc(employeeId, 1);
    }

    @Override
    @Transactional
    public List<EmployeeCertification> replaceCertifications(
            Long employeeId, List<EmployeeCertification> rows) {
        findEmployee(employeeId);
        List<EmployeeCertification> submittedRows =
                rows == null ? List.of() : rows;
        for (EmployeeCertification row : submittedRows) {
            validateCertification(row);
        }

        List<EmployeeCertification> current =
                employeeCertificationRepository
                        .findByEmployeeIdOrderByIdAsc(employeeId);
        Map<Long, EmployeeCertification> existingById = new HashMap<>();
        for (EmployeeCertification existing : current) {
            existingById.put(existing.getId(), existing);
        }

        Set<Long> handledIds = new HashSet<>();
        List<EmployeeCertification> newRows = new ArrayList<>();
        for (EmployeeCertification row : submittedRows) {
            if (row.getId() != null) {
                EmployeeCertification existing = existingById.get(row.getId());
                if (existing == null) {
                    throw new BadRequestException(
                            "Certification id " + row.getId()
                                    + " does not belong to this Employee");
                }
                copyCertification(row, existing);
                existing.setStatus(
                        row.getStatus() == null ? 1 : row.getStatus());
                handledIds.add(row.getId());
            } else {
                row.setId(null);
                row.setEmployeeId(employeeId);
                newRows.add(row);
            }
        }
        for (EmployeeCertification existing : current) {
            if (!handledIds.contains(existing.getId())) {
                existing.setStatus(0);
            }
        }

        employeeCertificationRepository.saveAll(current);
        employeeCertificationRepository.saveAll(newRows);
        return getCertifications(employeeId);
    }

    @Override
    public List<EmployeeJobExperience> getJobExperiences(Long employeeId) {
        findEmployee(employeeId);
        return employeeJobExperienceRepository
                .findByEmployeeIdAndStatusOrderByIdAsc(employeeId, 1);
    }

    @Override
    @Transactional
    public List<EmployeeJobExperience> replaceJobExperiences(
            Long employeeId, List<EmployeeJobExperience> rows) {
        findEmployee(employeeId);
        List<EmployeeJobExperience> submittedRows =
                rows == null ? List.of() : rows;
        for (EmployeeJobExperience row : submittedRows) {
            validateJobExperience(row);
        }

        List<EmployeeJobExperience> current =
                employeeJobExperienceRepository
                        .findByEmployeeIdOrderByIdAsc(employeeId);
        Map<Long, EmployeeJobExperience> existingById = new HashMap<>();
        for (EmployeeJobExperience existing : current) {
            existingById.put(existing.getId(), existing);
        }

        Set<Long> handledIds = new HashSet<>();
        List<EmployeeJobExperience> newRows = new ArrayList<>();
        for (EmployeeJobExperience row : submittedRows) {
            if (row.getId() != null) {
                EmployeeJobExperience existing = existingById.get(row.getId());
                if (existing == null) {
                    throw new BadRequestException(
                            "Job experience id " + row.getId()
                                    + " does not belong to this Employee");
                }
                copyJobExperience(row, existing);
                existing.setStatus(
                        row.getStatus() == null ? 1 : row.getStatus());
                handledIds.add(row.getId());
            } else {
                row.setId(null);
                row.setEmployeeId(employeeId);
                newRows.add(row);
            }
        }
        for (EmployeeJobExperience existing : current) {
            if (!handledIds.contains(existing.getId())) {
                existing.setStatus(0);
            }
        }

        employeeJobExperienceRepository.saveAll(current);
        employeeJobExperienceRepository.saveAll(newRows);
        return getJobExperiences(employeeId);
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));
    }

    private void validateEducation(EmployeeEducation row) {
        StatusUtil.validate(row.getStatus());

        if (row.getLevelOfEduId() == null) {
            throw new BadRequestException(
                    "Level of education cannot be empty");
        }
        LevelOfEducation levelOfEducation = levelOfEducationRepository
                .findById(row.getLevelOfEduId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Level of Education not found with id: "
                                + row.getLevelOfEduId()));
        if (!Integer.valueOf(1).equals(levelOfEducation.getStatus())) {
            throw new BadRequestException(
                    "Selected Level of Education is not active");
        }

        if (row.getDegreeId() == null) {
            throw new BadRequestException("Degree cannot be empty");
        }
        Degree degree = degreeRepository.findById(row.getDegreeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Degree not found with id: " + row.getDegreeId()));
        if (!Integer.valueOf(1).equals(degree.getStatus())) {
            throw new BadRequestException(
                    "Selected Degree is not active");
        }
        if (!degree.getLevelOfEduId().equals(row.getLevelOfEduId())) {
            throw new BadRequestException(
                    "Degree does not belong to selected Level of Education");
        }

        String majorGroup = ValidationUtil.optionalText(
                row.getMajorGroup(), "Major/Group", 150);
        String instituteName = ValidationUtil.requiredText(
                row.getInstituteName(), "Institute name", 2, 200);
        String result = ValidationUtil.requiredText(
                row.getResult(), "Result", 1, 50);
        BigDecimal cgpaMarks = ValidationUtil.positiveDecimal(
                row.getCgpaMarks(), "CGPA/Marks", false);
        BigDecimal scaleMarks = ValidationUtil.positiveDecimal(
                row.getScaleMarks(), "Scale/Marks", false);
        if (cgpaMarks != null && scaleMarks != null
                && cgpaMarks.compareTo(scaleMarks) > 0) {
            throw new BadRequestException(
                    "CGPA/Marks cannot exceed Scale/Marks");
        }
        Integer yearOfPassing = ValidationUtil.validYear(
                row.getYearOfPassing(), "Year of passing");
        BigDecimal duration = ValidationUtil.positiveDecimal(
                row.getDuration(), "Duration", false);

        row.setMajorGroup(majorGroup);
        row.setInstituteName(instituteName);
        row.setResult(result);
        row.setCgpaMarks(cgpaMarks);
        row.setScaleMarks(scaleMarks);
        row.setYearOfPassing(yearOfPassing);
        row.setDuration(duration);
    }

    private void copyEducation(
            EmployeeEducation source, EmployeeEducation target) {
        target.setLevelOfEduId(source.getLevelOfEduId());
        target.setDegreeId(source.getDegreeId());
        target.setMajorGroup(source.getMajorGroup());
        target.setInstituteName(source.getInstituteName());
        target.setResult(source.getResult());
        target.setCgpaMarks(source.getCgpaMarks());
        target.setScaleMarks(source.getScaleMarks());
        target.setYearOfPassing(source.getYearOfPassing());
        target.setDuration(source.getDuration());
    }

    private void validateTraining(
            EmployeeTraining row, Employee employee) {
        StatusUtil.validate(row.getStatus());
        row.setTrainingTitle(ValidationUtil.requiredText(
                row.getTrainingTitle(), "Training title", 2, 200));
        hrLookupValidator.validate(
                row.getCountryId(), employee.getBgId(), "Country", "Country");
        row.setTopicsCovered(ValidationUtil.requiredText(
                row.getTopicsCovered(), "Topics covered", 1, 500));
        row.setTrainingYear(ValidationUtil.validYear(
                row.getTrainingYear(), "Training year"));
        row.setInstituteName(ValidationUtil.requiredText(
                row.getInstituteName(), "Institute name", 2, 200));
        row.setDuration(ValidationUtil.positiveDecimal(
                row.getDuration(), "Duration", true));
        row.setLocation(ValidationUtil.requiredText(
                row.getLocation(), "Location", 1, 150));
    }

    private void copyTraining(
            EmployeeTraining source, EmployeeTraining target) {
        target.setTrainingTitle(source.getTrainingTitle());
        target.setCountryId(source.getCountryId());
        target.setTopicsCovered(source.getTopicsCovered());
        target.setTrainingYear(source.getTrainingYear());
        target.setInstituteName(source.getInstituteName());
        target.setDuration(source.getDuration());
        target.setLocation(source.getLocation());
    }

    private void validateCertification(EmployeeCertification row) {
        StatusUtil.validate(row.getStatus());
        row.setCertification(ValidationUtil.requiredText(
                row.getCertification(), "Certification", 2, 200));
        row.setInstituteName(ValidationUtil.requiredText(
                row.getInstituteName(), "Institute", 2, 200));
        row.setLocation(ValidationUtil.requiredText(
                row.getLocation(), "Location", 1, 150));
        row.setDuration(ValidationUtil.positiveDecimal(
                row.getDuration(), "Duration", true));
    }

    private void copyCertification(
            EmployeeCertification source, EmployeeCertification target) {
        target.setCertification(source.getCertification());
        target.setInstituteName(source.getInstituteName());
        target.setLocation(source.getLocation());
        target.setDuration(source.getDuration());
    }

    private void validateJobExperience(EmployeeJobExperience row) {
        StatusUtil.validate(row.getStatus());
        row.setCompanyName(ValidationUtil.requiredText(
                row.getCompanyName(), "Company name", 2, 200));
        row.setCompanyBusiness(ValidationUtil.requiredText(
                row.getCompanyBusiness(), "Company business", 2, 200));
        row.setDesignation(ValidationUtil.requiredText(
                row.getDesignation(), "Designation", 1, 150));
        row.setDepartment(ValidationUtil.requiredText(
                row.getDepartment(), "Department", 1, 200));

        if (row.getEmploymentPeriodStartDt() == null) {
            throw new BadRequestException(
                    "Employment start date cannot be empty");
        }
        if (row.getEmploymentPeriodStartDt().isAfter(java.time.LocalDate.now())) {
            throw new BadRequestException(
                    "Employment start date cannot be a future date");
        }

        Integer currentlyWorking = row.getCurrentlyWorking();
        if (currentlyWorking == null) {
            currentlyWorking = 0;
        }
        if (currentlyWorking != 0 && currentlyWorking != 1) {
            throw new BadRequestException(
                    "Currently working must be 0 or 1");
        }
        row.setCurrentlyWorking(currentlyWorking);

        if (currentlyWorking == 1
                && row.getEmploymentPeriodEndDt() != null) {
            throw new BadRequestException(
                    "End date must be empty when Currently working is checked");
        }
        if (row.getEmploymentPeriodEndDt() != null
                && row.getEmploymentPeriodEndDt()
                .isBefore(row.getEmploymentPeriodStartDt())) {
            throw new BadRequestException(
                    "Employment end date cannot be before start date");
        }
        if (row.getEmploymentPeriodEndDt() != null
                && row.getEmploymentPeriodEndDt()
                .isAfter(java.time.LocalDate.now())) {
            throw new BadRequestException(
                    "Employment end date cannot be a future date");
        }
        row.setResponsibilities(ValidationUtil.optionalText(
                row.getResponsibilities(), "Responsibilities", 500));
    }

    private void copyJobExperience(
            EmployeeJobExperience source, EmployeeJobExperience target) {
        target.setCompanyName(source.getCompanyName());
        target.setCompanyBusiness(source.getCompanyBusiness());
        target.setDesignation(source.getDesignation());
        target.setDepartment(source.getDepartment());
        target.setEmploymentPeriodStartDt(
                source.getEmploymentPeriodStartDt());
        target.setEmploymentPeriodEndDt(source.getEmploymentPeriodEndDt());
        target.setCurrentlyWorking(source.getCurrentlyWorking());
        target.setResponsibilities(source.getResponsibilities());
    }
}
