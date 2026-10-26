package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "hr_employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Required =====
    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "employee_name", nullable = false, length = 150)
    private String employeeName;

    @Column(name = "employee_code", length = 50, unique = true)
    private String employeeCode;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "desig_id", nullable = false)
    private Long desigId;

    // ===== Optional =====
    @Column(name = "photo", length = 255)
    private String photo;

    @Column(name = "employment_type_id")
    private Long employmentTypeId;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "employment_status_id")
    private Long employmentStatusId;

    @Column(name = "probationary_period")
    private Integer probationaryPeriod;

    @Column(name = "notice_period")
    private Integer noticePeriod;

    @Column(name = "separation_date")
    private LocalDate separationDate;

    @Column(name = "office_in_time")
    private LocalTime officeInTime;

    @Column(name = "office_out_time")
    private LocalTime officeOutTime;

    @Column(name = "overtime_allowance")
    private Boolean overtimeAllowance;

    @Column(name = "reporting_officer_id")
    private Long reportingOfficerId;

    @Column(name = "father_name", length = 150)
    private String fatherName;

    @Column(name = "mother_name", length = 150)
    private String motherName;

    @Column(name = "spouse_name", length = 150)
    private String spouseName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "birth_place", length = 150)
    private String birthPlace;

    // Text (lookup id na)
    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "religion", length = 50)
    private String religion;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Column(name = "date_of_marriage")
    private LocalDate dateOfMarriage;

    @Column(name = "nationality_id")
    private Long nationalityId;

    @Column(name = "highest_education_id")
    private Long highestEducationId;

    @Column(name = "height", precision = 5, scale = 2)
    private BigDecimal height;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "nid", length = 20)
    private String nid;

    @Column(name = "passport_no", length = 20)
    private String passportNo;

    @Column(name = "driving_license_no", length = 30)
    private String drivingLicenseNo;

    @Column(name = "no_of_child")
    private Integer noOfChild;

    @Column(name = "male_child")
    private Integer maleChild;

    @Column(name = "female_child")
    private Integer femaleChild;

    @Column(name = "present_address", length = 500)
    private String presentAddress;

    @Column(name = "permanent_address", length = 500)
    private String permanentAddress;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "thana", length = 100)
    private String thana;

    @Column(name = "office_mobile_no", length = 20)
    private String officeMobileNo;

    @Column(name = "personal_mobile_no", length = 20)
    private String personalMobileNo;

    @Column(name = "whatsapp_no", length = 20)
    private String whatsappNo;

    @Column(name = "residence_phone", length = 20)
    private String residencePhone;

    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;

    @Column(name = "office_email", length = 150)
    private String officeEmail;

    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Column(name = "disability_status", nullable = false,
            columnDefinition = "TINYINT NOT NULL DEFAULT 0")
    private Integer disabilityStatus;

    @Column(name = "disability_type_id")
    private Long disabilityTypeId;

    @Column(name = "disability_details", length = 500)
    private String disabilityDetails;

    @Column(name = "has_national_disability_id", nullable = false,
            columnDefinition = "TINYINT NOT NULL DEFAULT 0")
    private Integer hasNationalDisabilityId;

    @Column(name = "national_disability_id", length = 30)
    private String nationalDisabilityId;
}