package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.common.ValidationUtil;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final String BD_MOBILE = "^01[0-9]{9}$";
    private static final String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

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
        validateDisability(source, target, selfId);
    }

    // =========================================
    // OFFICE (required: BG/BU, name, department, designation)
    // =========================================
    // =========================================
    // OFFICE (required: BG/BU, name, department, designation)
    // =========================================
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

        // Department (required)
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

        // Designation (required)
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

        // Employment Type (optional)
        if (source.getEmploymentTypeId() != null) {
            EmploymentType employmentType = employmentTypeRepository
                    .findById(source.getEmploymentTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employment Type not found with id: "
                                    + source.getEmploymentTypeId()));
            if (!Integer.valueOf(1).equals(employmentType.getStatus())) {
                throw new BadRequestException(
                        "Selected Employment Type is not active");
            }
        }

        // Employment Status (optional)
        boolean separated = false;
        if (source.getEmploymentStatusId() != null) {
            EmploymentStatus employmentStatus = employmentStatusRepository
                    .findById(source.getEmploymentStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employment Status not found with id: "
                                    + source.getEmploymentStatusId()));
            if (!Integer.valueOf(1).equals(employmentStatus.getStatus())) {
                throw new BadRequestException(
                        "Selected Employment Status is not active");
            }
            // BU match (purono BU-less row-e skip)
            if (employmentStatus.getBuId() != null
                    && !buId.equals(employmentStatus.getBuId())) {
                throw new BadRequestException(
                        "Employment Status does not belong to selected Business Unit");
            }
            String statusName = employmentStatus.getStatusName();
            separated = statusName != null
                    && (statusName.equalsIgnoreCase("Resigned")
                    || statusName.equalsIgnoreCase("Terminated")
                    || statusName.equalsIgnoreCase("Retired"));
        }

        // Dates (optional, order check shudhu duita thakle)
        LocalDate today = LocalDate.now();
        if (source.getJoiningDate() != null
                && source.getJoiningDate().isAfter(today)) {
            throw new BadRequestException(
                    "Joining date cannot be a future date");
        }
        if (source.getConfirmationDate() != null
                && source.getJoiningDate() != null
                && source.getConfirmationDate().isBefore(source.getJoiningDate())) {
            throw new BadRequestException(
                    "Confirmation date cannot be before joining date");
        }
        if (separated && source.getSeparationDate() == null) {
            throw new BadRequestException(
                    "Separation date is required for the selected Employment Status");
        }
        if (source.getSeparationDate() != null
                && source.getJoiningDate() != null
                && source.getSeparationDate().isBefore(source.getJoiningDate())) {
            throw new BadRequestException(
                    "Separation date cannot be before joining date");
        }
        LocalDate separationDate = separated
                ? source.getSeparationDate() : null;

        Integer probationaryPeriod = optInt(
                source.getProbationaryPeriod(), "Probationary period", 1, 24);
        Integer noticePeriod = optInt(
                source.getNoticePeriod(), "Notice period", 0, 12);

        if (source.getOfficeInTime() != null
                && source.getOfficeOutTime() != null
                && !source.getOfficeOutTime().isAfter(source.getOfficeInTime())) {
            throw new BadRequestException(
                    "Office out time must be later than office in time");
        }

        // Reporting officer (optional)
        Long reportingOfficerId = source.getReportingOfficerId();
        if (reportingOfficerId != null) {
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
        target.setOfficeInTime(source.getOfficeInTime());
        target.setOfficeOutTime(source.getOfficeOutTime());
        target.setOvertimeAllowance(source.getOvertimeAllowance());
        target.setReportingOfficerId(reportingOfficerId);
    }

    // =========================================
    // PERSONAL (shob optional)
    // =========================================
    private void validatePersonal(
            Employee source, Employee target, Long selfId) {
        LocalDate today = LocalDate.now();

        String fatherName = optText(
                source.getFatherName(), "Father's name", 2, 150);
        String motherName = optText(
                source.getMotherName(), "Mother's name", 2, 150);
        String birthPlace = optText(
                source.getBirthPlace(), "Birth place", 1, 150);

        // Text field, jemon ashe temon save (shudhu length check)
        String gender = optText(source.getGender(), "Gender", 1, 20);
        String religion = optText(source.getReligion(), "Religion", 1, 50);
        String bloodGroup = optText(
                source.getBloodGroup(), "Blood group", 1, 10);

        // Marital status (optional)
        String canonicalMaritalStatus = null;
        if (source.getMaritalStatus() != null
                && !source.getMaritalStatus().trim().isEmpty()) {
            String v = source.getMaritalStatus().trim();
            for (String value : List.of(
                    "Single", "Married", "Divorced", "Widowed")) {
                if (value.equalsIgnoreCase(v)) {
                    canonicalMaritalStatus = value;
                    break;
                }
            }
            if (canonicalMaritalStatus == null) {
                throw new BadRequestException(
                        "Marital status must be Single, Married, Divorced or Widowed");
            }
        }

        // Date of birth (optional)
        if (source.getDateOfBirth() != null) {
            if (source.getDateOfBirth().isAfter(today)) {
                throw new BadRequestException(
                        "Date of birth cannot be a future date");
            }
            if (Period.between(source.getDateOfBirth(), today).getYears() < 18) {
                throw new BadRequestException(
                        "Employee must be at least 18 years old");
            }
        }

        // Spouse (Married hole required)
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
            if (source.getDateOfBirth() != null
                    && !dateOfMarriage.isAfter(source.getDateOfBirth())) {
                throw new BadRequestException(
                        "Date of marriage must be after date of birth");
            }
        }

        // Nationality (lookup id, optional)
        if (source.getNationalityId() != null) {
            hrLookupValidator.validate(
                    source.getNationalityId(), source.getBgId(),
                    "Nationality", "Nationality");
        }

        // Highest education (optional)
        if (source.getHighestEducationId() != null) {
            LevelOfEducation education = levelOfEducationRepository
                    .findById(source.getHighestEducationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Highest Education not found with id: "
                                    + source.getHighestEducationId()));
            if (!Integer.valueOf(1).equals(education.getStatus())) {
                throw new BadRequestException(
                        "Selected Highest Education is not active");
            }
        }

        // Height / Weight (optional)
        BigDecimal height = optDecimal(source.getHeight(), "Height",
                new BigDecimal("30"), new BigDecimal("300"));
        BigDecimal weight = optDecimal(source.getWeight(), "Weight",
                new BigDecimal("1"), new BigDecimal("500"));

        // IDs (optional, dile format + unique)
        String nid = ValidationUtil.optionalPattern(
                source.getNid(), "National ID", 17,
                "^([0-9]{10}|[0-9]{13}|[0-9]{17})$",
                "National ID must be 10, 13 or 17 digits");
        if (nid != null && employeeRepository.findByNid(nid).stream()
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

        // Children (optional; tinta-i dile sum mile thakte hobe)
        Integer noOfChild = optInt(source.getNoOfChild(), "No. of child", 0, 30);
        Integer maleChild = optInt(source.getMaleChild(), "Male child", 0, 30);
        Integer femaleChild = optInt(
                source.getFemaleChild(), "Female child", 0, 30);
        if (noOfChild != null && maleChild != null && femaleChild != null
                && maleChild + femaleChild != noOfChild) {
            throw new BadRequestException(
                    "Male child + Female child must equal No. of child");
        }

        target.setFatherName(fatherName);
        target.setMotherName(motherName);
        target.setSpouseName(spouseName);
        target.setDateOfBirth(source.getDateOfBirth());
        target.setBirthPlace(birthPlace);
        target.setGender(gender);
        target.setReligion(religion);
        target.setBloodGroup(bloodGroup);
        target.setMaritalStatus(canonicalMaritalStatus);
        target.setDateOfMarriage(dateOfMarriage);
        target.setNationalityId(source.getNationalityId());
        target.setHighestEducationId(source.getHighestEducationId());
        target.setHeight(height);
        target.setWeight(weight);
        target.setNid(nid);
        target.setPassportNo(passportNo);
        target.setDrivingLicenseNo(drivingLicenseNo);
        target.setNoOfChild(noOfChild);
        target.setMaleChild(maleChild);
        target.setFemaleChild(femaleChild);
    }

    // =========================================
    // CONTACT (shob optional)
    // =========================================
    private void validateContact(
            Employee source, Employee target, Long selfId) {
        String presentAddress = optText(
                source.getPresentAddress(), "Present address", 10, 500);
        String permanentAddress = optText(
                source.getPermanentAddress(), "Permanent address", 10, 500);
        String district = optText(source.getDistrict(), "District", 1, 100);
        String thana = optText(source.getThana(), "Thana", 1, 100);

        // Office mobile (unique)
        String officeMobileNo = ValidationUtil.optionalPattern(
                source.getOfficeMobileNo(), "Office mobile no", 11,
                BD_MOBILE,
                "Invalid office mobile number. Must be 11 digits starting with 01");
        if (officeMobileNo != null
                && employeeRepository.findByOfficeMobileNo(officeMobileNo)
                .stream()
                .anyMatch(x -> selfId == null || !x.getId().equals(selfId))) {
            throw new DuplicateResourceException(
                    "Office mobile no already exists: " + officeMobileNo);
        }

        // Personal mobile (unique na)
        String personalMobileNo = ValidationUtil.optionalPattern(
                source.getPersonalMobileNo(), "Personal mobile no", 11,
                BD_MOBILE,
                "Invalid personal mobile number. Must be 11 digits starting with 01");

        String whatsappNo = ValidationUtil.optionalPattern(
                source.getWhatsappNo(), "WhatsApp no", 11, BD_MOBILE,
                "Invalid WhatsApp number. Must be 11 digits starting with 01");
        String residencePhone = ValidationUtil.optionalPattern(
                source.getResidencePhone(), "Residence phone", 15,
                "^[0-9]{6,15}$",
                "Residence phone must be 6 to 15 digits");
        String emergencyContact = ValidationUtil.optionalPattern(
                source.getEmergencyContact(), "Emergency contact", 20,
                "^[0-9+() -]{6,20}$",
                "Emergency contact allows only digits, +, (, ), space and hyphen (6 to 20 characters)");

        // Office email (unique)
        String officeEmail = ValidationUtil.optionalPattern(
                source.getOfficeEmail(), "Office email", 150, EMAIL,
                "Invalid office email format");
        if (officeEmail != null) {
            officeEmail = officeEmail.toLowerCase(Locale.ROOT);
            final String oe = officeEmail;
            if (employeeRepository.findByOfficeEmailIgnoreCase(oe).stream()
                    .anyMatch(x -> selfId == null
                            || !x.getId().equals(selfId))) {
                throw new DuplicateResourceException(
                        "Office email already exists: " + oe);
            }
        }

        // Personal email (unique na)
        String personalEmail = ValidationUtil.optionalPattern(
                source.getPersonalEmail(), "Personal email", 150, EMAIL,
                "Invalid personal email format");
        if (personalEmail != null) {
            personalEmail = personalEmail.toLowerCase(Locale.ROOT);
        }

        target.setPresentAddress(presentAddress);
        target.setPermanentAddress(permanentAddress);
        target.setDistrict(district);
        target.setThana(thana);
        target.setOfficeMobileNo(officeMobileNo);
        target.setPersonalMobileNo(personalMobileNo);
        target.setWhatsappNo(whatsappNo);
        target.setResidencePhone(residencePhone);
        target.setEmergencyContact(emergencyContact);
        target.setOfficeEmail(officeEmail);
        target.setPersonalEmail(personalEmail);
    }

    // =========================================
    // DISABILITY
    // =========================================
    private void validateDisability(
            Employee src, Employee target, Long selfId) {

        int status = src.getDisabilityStatus() == null
                ? 0 : src.getDisabilityStatus();
        if (status != 0 && status != 1) {
            throw new BadRequestException(
                    "Disability status must be 0 or 1");
        }

        // Status No hole baki shob clear
        if (status == 0) {
            target.setDisabilityStatus(0);
            target.setDisabilityTypeId(null);
            target.setDisabilityDetails(null);
            target.setHasNationalDisabilityId(0);
            target.setNationalDisabilityId(null);
            return;
        }

        hrLookupValidator.validate(
                src.getDisabilityTypeId(), src.getBgId(),
                "Disability Type", "Disability type");

        String details = null;
        if (src.getDisabilityDetails() != null
                && !src.getDisabilityDetails().trim().isEmpty()) {
            details = src.getDisabilityDetails().trim();
            if (details.length() > 500) {
                throw new BadRequestException(
                        "Disability details cannot exceed 500 characters");
            }
            if (details.indexOf('<') >= 0 || details.indexOf('>') >= 0) {
                throw new BadRequestException(
                        "Disability details contain unsupported characters");
            }
        }

        Integer hasId = src.getHasNationalDisabilityId();
        if (hasId == null) {
            throw new BadRequestException(
                    "National disability ID No (Yes/No) cannot be empty");
        }
        if (hasId != 0 && hasId != 1) {
            throw new BadRequestException(
                    "National disability ID No must be 0 or 1");
        }

        String ndid = null;
        if (hasId == 1) {
            if (src.getNationalDisabilityId() == null
                    || src.getNationalDisabilityId().trim().isEmpty()) {
                throw new BadRequestException(
                        "National disability ID cannot be empty");
            }
            ndid = src.getNationalDisabilityId().trim();
            if (ndid.length() < 5 || ndid.length() > 30) {
                throw new BadRequestException(
                        "National disability ID must be between 5 and 30 characters");
            }
            if (!ndid.matches("^[A-Za-z0-9-]+$")) {
                throw new BadRequestException(
                        "National disability ID allows only letters, digits and hyphen");
            }
            final String value = ndid;
            boolean duplicate = employeeRepository
                    .findByNationalDisabilityIdIgnoreCase(value).stream()
                    .anyMatch(x -> selfId == null
                            || !x.getId().equals(selfId));
            if (duplicate) {
                throw new DuplicateResourceException(
                        "National disability ID already exists: " + value);
            }
        }

        target.setDisabilityStatus(1);
        target.setDisabilityTypeId(src.getDisabilityTypeId());
        target.setDisabilityDetails(details);
        target.setHasNationalDisabilityId(hasId);
        target.setNationalDisabilityId(ndid);
    }

    // =========================================
    // COMMON
    // =========================================
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

    // Optional text: blank hole null, dile min/max check
    private String optText(String v, String label, int min, int max) {
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        String t = v.trim();
        if (t.length() < min) {
            throw new BadRequestException(
                    label + " must be at least " + min + " characters");
        }
        if (t.length() > max) {
            throw new BadRequestException(
                    label + " cannot exceed " + max + " characters");
        }
        return t;
    }

    // Optional integer: null hole null, dile range check
    private Integer optInt(Integer v, String label, int min, int max) {
        if (v == null) {
            return null;
        }
        if (v < min || v > max) {
            throw new BadRequestException(
                    label + " must be between " + min + " and " + max);
        }
        return v;
    }

    // Optional decimal: range + max 2 decimal place
    private BigDecimal optDecimal(
            BigDecimal v, String label, BigDecimal min, BigDecimal max) {
        if (v == null) {
            return null;
        }
        if (v.compareTo(min) < 0 || v.compareTo(max) > 0) {
            throw new BadRequestException(
                    label + " must be between " + min.toPlainString()
                            + " and " + max.toPlainString());
        }
        if (v.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException(
                    label + " allows maximum 2 decimal places");
        }
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}