package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.ValidationUtil;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.entity.Designation;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.entity.EmploymentStatus;
import com.tse.erp.module.hr.entity.EmploymentType;
import com.tse.erp.module.hr.entity.LevelOfEducation;
import com.tse.erp.module.hr.repository.DepartmentGroupRepository;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import com.tse.erp.module.hr.repository.DeptDesigMapRepository;
import com.tse.erp.module.hr.repository.DesignationGroupRepository;
import com.tse.erp.module.hr.repository.DesignationRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.hr.repository.EmploymentStatusRepository;
import com.tse.erp.module.hr.repository.EmploymentTypeRepository;
import com.tse.erp.module.hr.repository.LevelOfEducationRepository;
import com.tse.erp.module.hr.service.EmployeeService;
import com.tse.erp.module.hr.validation.HrLookupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentGroupRepository departmentGroupRepository;
    private final DesignationRepository designationRepository;
    private final DesignationGroupRepository designationGroupRepository;
    private final DeptDesigMapRepository deptDesigMapRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final EmploymentStatusRepository employmentStatusRepository;
    private final LevelOfEducationRepository levelOfEducationRepository;
    private final HrLookupValidator hrLookupValidator;

    @Override
    public List<Employee> getAllEmployees(Long buId) {
        if (buId == null) {
            return employeeRepository.findAllByOrderByIdDesc();
        }
        return employeeRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + id));
    }

    @Override
    @Transactional
    public Employee createEmployee(Employee employee) {
        StatusUtil.validate(employee.getStatus());
        Employee target = new Employee();
        validateAndApply(target, employee, null);
        if (employee.getStatus() != null) {
            target.setStatus(employee.getStatus());
        }
        Employee saved = employeeRepository.save(target);
        saved.setEmployeeCode(
                "EMP-" + String.format("%06d", saved.getId()));
        return employeeRepository.save(saved);
    }

    @Override
    @Transactional
    public Employee updateEmployee(Long id, Employee employee) {
        Employee existing = getEmployeeById(id);
        StatusUtil.validate(employee.getStatus());
        validateAndApply(existing, employee, id);
        if (employee.getStatus() != null) {
            existing.setStatus(employee.getStatus());
        }
        return employeeRepository.save(existing);
    }

    private void validateAndApply(
            Employee target, Employee source, Long selfId) {
        validateOffice(source, target, selfId);
        validatePersonal(source, target, selfId);
        validateContact(source, target, selfId);
    }

    private void validateOffice(
            Employee source, Employee target, Long selfId) {
        validateParents(source.getBgId(), source.getBuId());

        Long bgId = source.getBgId();
        Long buId = source.getBuId();
        String employeeName = ValidationUtil.requiredPattern(
                source.getEmployeeName(), "Employee name", 2, 150,
                "^[\\p{L} .'-]+$",
                "Employee name allows only letters, space, dot, hyphen and apostrophe");
        String photo = ValidationUtil.optionalText(
                source.getPhoto(), "Photo", 255);

        if (source.getDeptId() == null) {
            throw new BadRequestException("Department cannot be empty");
        }
        Department department = departmentRepository.findById(source.getDeptId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + source.getDeptId()));
        if (!Integer.valueOf(1).equals(department.getStatus())) {
            throw new BadRequestException("Selected Department is not active");
        }
        if (!bgId.equals(department.getBgId())) {
            throw new BadRequestException(
                    "Department does not belong to selected Business Group");
        }
        if (departmentGroupRepository.findByDeptIdAndBuId(
                source.getDeptId(), buId).isEmpty()) {
            throw new BadRequestException(
                    "Department is not mapped to selected Business Unit");
        }

        if (source.getDesigId() == null) {
            throw new BadRequestException("Designation cannot be empty");
        }
        Designation designation = designationRepository
                .findById(source.getDesigId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found with id: " + source.getDesigId()));
        if (!Integer.valueOf(1).equals(designation.getStatus())) {
            throw new BadRequestException(
                    "Selected Designation is not active");
        }
        if (!bgId.equals(designation.getBgId())) {
            throw new BadRequestException(
                    "Designation does not belong to selected Business Group");
        }
        if (designationGroupRepository.findByDesigIdAndBuId(
                source.getDesigId(), buId).isEmpty()) {
            throw new BadRequestException(
                    "Designation is not mapped to selected Business Unit");
        }
        if (deptDesigMapRepository.findByBuIdAndDeptIdAndDesigId(
                buId, source.getDeptId(), source.getDesigId()).isEmpty()) {
            throw new BadRequestException(
                    "Designation is not mapped to selected Department");
        }

        if (source.getEmploymentTypeId() == null) {
            throw new BadRequestException(
                    "Employment type cannot be empty");
        }
        EmploymentType employmentType = employmentTypeRepository
                .findById(source.getEmploymentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment Type not found with id: "
                                + source.getEmploymentTypeId()));
        if (!Integer.valueOf(1).equals(employmentType.getStatus())) {
            throw new BadRequestException(
                    "Selected Employment Type is not active");
        }

        if (source.getEmploymentStatusId() == null) {
            throw new BadRequestException(
                    "Employment status cannot be empty");
        }
        EmploymentStatus employmentStatus = employmentStatusRepository
                .findById(source.getEmploymentStatusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment Status not found with id: "
                                + source.getEmploymentStatusId()));
        if (!Integer.valueOf(1).equals(employmentStatus.getStatus())) {
            throw new BadRequestException(
                    "Selected Employment Status is not active");
        }

        LocalDate today = LocalDate.now();
        if (source.getJoiningDate() == null) {
            throw new BadRequestException("Joining date cannot be empty");
        }
        if (source.getJoiningDate().isAfter(today)) {
            throw new BadRequestException(
                    "Joining date cannot be a future date");
        }
        if (source.getConfirmationDate() != null
                && source.getConfirmationDate().isBefore(source.getJoiningDate())) {
            throw new BadRequestException(
                    "Confirmation date cannot be before joining date");
        }

        Integer probationaryPeriod = ValidationUtil.requiredInt(
                source.getProbationaryPeriod(), "Probationary period", 1, 24);
        Integer noticePeriod = ValidationUtil.requiredInt(
                source.getNoticePeriod(), "Notice period", 0, 12);

        String statusName = employmentStatus.getStatusName();
        boolean separated = statusName != null
                && (statusName.equalsIgnoreCase("Resigned")
                || statusName.equalsIgnoreCase("Terminated")
                || statusName.equalsIgnoreCase("Retired"));
        if (separated && source.getSeparationDate() == null) {
            throw new BadRequestException(
                    "Separation date is required for the selected Employment Status");
        }
        if (source.getSeparationDate() != null
                && source.getSeparationDate().isBefore(source.getJoiningDate())) {
            throw new BadRequestException(
                    "Separation date cannot be before joining date");
        }
        LocalDate separationDate = separated
                ? source.getSeparationDate() : null;

        if (source.getShiftId() == null) {
            throw new BadRequestException("Shift cannot be empty");
        }
        // TODO: validate against Shift master when it exists
        if (source.getOfficeInTime() == null) {
            throw new BadRequestException(
                    "Office in time cannot be empty");
        }
        if (source.getOfficeOutTime() == null) {
            throw new BadRequestException(
                    "Office out time cannot be empty");
        }
        if (!source.getOfficeOutTime().isAfter(source.getOfficeInTime())) {
            throw new BadRequestException(
                    "Office out time must be later than office in time");
        }
        if (source.getOvertimeAllowance() == null) {
            throw new BadRequestException(
                    "Overtime allowance cannot be empty");
        }

        Long reportingOfficerId = source.getReportingOfficerId();
        if (employeeRepository.count() > 0 || selfId != null) {
            if (reportingOfficerId == null) {
                throw new BadRequestException(
                        "Reporting officer cannot be empty");
            }
            Employee reportingOfficer = employeeRepository
                    .findById(reportingOfficerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Reporting officer not found with id: "
                                    + reportingOfficerId));
            if (!Integer.valueOf(1).equals(reportingOfficer.getStatus())) {
                throw new BadRequestException(
                        "Selected Reporting officer is not active");
            }
            if (selfId != null && selfId.equals(reportingOfficerId)) {
                throw new BadRequestException(
                        "Employee cannot be own reporting officer");
            }
        }

        target.setBgId(bgId);
        target.setBuId(buId);
        target.setEmployeeName(employeeName);
        target.setPhoto(photo);
        target.setDeptId(source.getDeptId());
        target.setDesigId(source.getDesigId());
        target.setEmploymentTypeId(source.getEmploymentTypeId());
        target.setJoiningDate(source.getJoiningDate());
        target.setConfirmationDate(source.getConfirmationDate());
        target.setEmploymentStatusId(source.getEmploymentStatusId());
        target.setProbationaryPeriod(probationaryPeriod);
        target.setNoticePeriod(noticePeriod);
        target.setSeparationDate(separationDate);
        target.setShiftId(source.getShiftId());
        target.setOfficeInTime(source.getOfficeInTime());
        target.setOfficeOutTime(source.getOfficeOutTime());
        target.setOvertimeAllowance(source.getOvertimeAllowance());
        target.setReportingOfficerId(reportingOfficerId);
    }

    private void validatePersonal(
            Employee source, Employee target, Long selfId) {
        Long bgId = source.getBgId();
        String fatherName = ValidationUtil.requiredText(
                source.getFatherName(), "Father's name", 2, 150);
        String motherName = ValidationUtil.requiredText(
                source.getMotherName(), "Mother's name", 2, 150);

        String maritalStatus = ValidationUtil.requiredText(
                source.getMaritalStatus(), "Marital status", 1, 20);
        String canonicalMaritalStatus = null;
        for (String value : List.of(
                "Single", "Married", "Divorced", "Widowed")) {
            if (value.equalsIgnoreCase(maritalStatus)) {
                canonicalMaritalStatus = value;
                break;
            }
        }
        if (canonicalMaritalStatus == null) {
            throw new BadRequestException(
                    "Marital status must be Single, Married, Divorced or Widowed");
        }

        LocalDate today = LocalDate.now();
        if (source.getDateOfBirth() == null) {
            throw new BadRequestException("Date of birth cannot be empty");
        }
        if (source.getDateOfBirth().isAfter(today)) {
            throw new BadRequestException(
                    "Date of birth cannot be a future date");
        }
        if (Period.between(source.getDateOfBirth(), today).getYears() < 18) {
            throw new BadRequestException(
                    "Employee must be at least 18 years old");
        }

        String birthPlace = ValidationUtil.requiredText(
                source.getBirthPlace(), "Birth place", 1, 150);
        String spouseName = null;
        LocalDate dateOfMarriage = null;
        if ("Married".equals(canonicalMaritalStatus)) {
            spouseName = ValidationUtil.requiredText(
                    source.getSpouseName(), "Spouse name", 2, 150);
            dateOfMarriage = source.getDateOfMarriage();
            if (dateOfMarriage == null) {
                throw new BadRequestException(
                        "Date of marriage is required for married employee");
            }
            if (dateOfMarriage.isAfter(today)) {
                throw new BadRequestException(
                        "Date of marriage cannot be a future date");
            }
            if (!dateOfMarriage.isAfter(source.getDateOfBirth())) {
                throw new BadRequestException(
                        "Date of marriage must be after date of birth");
            }
        }

        hrLookupValidator.validate(
                source.getGenderId(), bgId, "Gender", "Gender");
        hrLookupValidator.validate(
                source.getReligionId(), bgId, "Religion", "Religion");
        hrLookupValidator.validate(
                source.getNationalityId(), bgId, "Nationality", "Nationality");
        hrLookupValidator.validate(
                source.getBloodGroupId(), bgId, "Blood Group", "Blood group");

        if (source.getHighestEducationId() == null) {
            throw new BadRequestException(
                    "Highest education cannot be empty");
        }
        LevelOfEducation education = levelOfEducationRepository
                .findById(source.getHighestEducationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Highest Education not found with id: "
                                + source.getHighestEducationId()));
        if (!Integer.valueOf(1).equals(education.getStatus())) {
            throw new BadRequestException(
                    "Selected Highest Education is not active");
        }

        String nid = ValidationUtil.requiredPattern(
                source.getNid(), "National ID", 10, 17,
                "^([0-9]{10}|[0-9]{13}|[0-9]{17})$",
                "National ID must be 10, 13 or 17 digits");
        if (employeeRepository.findByNid(nid).stream()
                .anyMatch(x -> selfId == null || !x.getId().equals(selfId))) {
            throw new DuplicateResourceException(
                    "National ID already exists: " + nid);
        }

        String passportNo = ValidationUtil.optionalPattern(
                source.getPassportNo(), "Passport no", 20,
                "^[A-Za-z0-9]+$", "Passport no must be alphanumeric");
        if (passportNo != null && employeeRepository
                .findByPassportNoIgnoreCase(passportNo).stream()
                .anyMatch(x -> selfId == null || !x.getId().equals(selfId))) {
            throw new DuplicateResourceException(
                    "Passport no already exists: " + passportNo);
        }

        String drivingLicenseNo = ValidationUtil.optionalPattern(
                source.getDrivingLicenseNo(), "Driving license no", 30,
                "^[A-Za-z0-9-]+$",
                "Driving license no allows only letters, digits and hyphen");
        if (drivingLicenseNo != null && employeeRepository
                .findByDrivingLicenseNoIgnoreCase(drivingLicenseNo).stream()
                .anyMatch(x -> selfId == null || !x.getId().equals(selfId))) {
            throw new DuplicateResourceException(
                    "Driving license no already exists: " + drivingLicenseNo);
        }

        Integer noOfChild = ValidationUtil.requiredInt(
                source.getNoOfChild(), "No. of child", 0, 30);
        Integer maleChild = ValidationUtil.requiredInt(
                source.getMaleChild(), "Male child", 0, 30);
        Integer femaleChild = ValidationUtil.requiredInt(
                source.getFemaleChild(), "Female child", 0, 30);
        if (maleChild + femaleChild != noOfChild) {
            throw new BadRequestException(
                    "Male child + Female child must equal No. of child");
        }

        target.setFatherName(fatherName);
        target.setMotherName(motherName);
        target.setSpouseName(spouseName);
        target.setDateOfBirth(source.getDateOfBirth());
        target.setBirthPlace(birthPlace);
        target.setGenderId(source.getGenderId());
        target.setMaritalStatus(canonicalMaritalStatus);
        target.setDateOfMarriage(dateOfMarriage);
        target.setReligionId(source.getReligionId());
        target.setNationalityId(source.getNationalityId());
        target.setBloodGroupId(source.getBloodGroupId());
        target.setHighestEducationId(source.getHighestEducationId());
        target.setNid(nid);
        target.setPassportNo(passportNo);
        target.setDrivingLicenseNo(drivingLicenseNo);
        target.setNoOfChild(noOfChild);
        target.setMaleChild(maleChild);
        target.setFemaleChild(femaleChild);
    }

    private void validateContact(
            Employee source, Employee target, Long selfId) {
        String presentAddress = ValidationUtil.requiredText(
                source.getPresentAddress(), "Present address", 10, 500);
        String permanentAddress = ValidationUtil.requiredText(
                source.getPermanentAddress(), "Permanent address", 10, 500);

        String mobileNo = ValidationUtil.requiredPattern(
                source.getMobileNo(), "Mobile no", 11, 11,
                "^01[0-9]{9}$",
                "Invalid mobile number. Must be 11 digits starting with 01");
        if (employeeRepository.findByMobileNo(mobileNo).stream()
                .anyMatch(x -> selfId == null || !x.getId().equals(selfId))) {
            throw new DuplicateResourceException(
                    "Mobile no already exists: " + mobileNo);
        }

        String whatsappNo = ValidationUtil.optionalPattern(
                source.getWhatsappNo(), "WhatsApp no", 11,
                "^01[0-9]{9}$",
                "Invalid WhatsApp number. Must be 11 digits starting with 01");
        String residencePhone = ValidationUtil.optionalPattern(
                source.getResidencePhone(), "Residence phone", 15,
                "^[0-9]{6,15}$",
                "Residence phone must be 6 to 15 digits");

        String email = ValidationUtil.requiredPattern(
                source.getEmail(), "Email", 5, 150,
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
                "Invalid email format").toLowerCase(Locale.ROOT);
        if (employeeRepository.findByEmailIgnoreCase(email).stream()
                .anyMatch(x -> selfId == null || !x.getId().equals(selfId))) {
            throw new DuplicateResourceException(
                    "Email already exists: " + email);
        }

        String fax = ValidationUtil.optionalPattern(
                source.getFax(), "Fax", 30, "^[0-9+() -]{5,30}$",
                "Fax allows only digits, +, (, ), space and hyphen");

        target.setPresentAddress(presentAddress);
        target.setPermanentAddress(permanentAddress);
        target.setMobileNo(mobileNo);
        target.setWhatsappNo(whatsappNo);
        target.setResidencePhone(residencePhone);
        target.setEmail(email);
        target.setFax(fax);
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));

        BusinessUnit businessUnit = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + buId));
        if (!bgId.equals(businessUnit.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }
    }
}
