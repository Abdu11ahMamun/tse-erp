package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "shift_id")
    private Long shiftId;

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

    @Column(name = "gender_id")
    private Long genderId;

    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Column(name = "date_of_marriage")
    private LocalDate dateOfMarriage;

    @Column(name = "religion_id")
    private Long religionId;

    @Column(name = "nationality_id")
    private Long nationalityId;

    @Column(name = "blood_group_id")
    private Long bloodGroupId;

    @Column(name = "highest_education_id")
    private Long highestEducationId;

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

    @Column(name = "mobile_no", length = 20)
    private String mobileNo;

    @Column(name = "whatsapp_no", length = 20)
    private String whatsappNo;

    @Column(name = "residence_phone", length = 20)
    private String residencePhone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "fax", length = 30)
    private String fax;

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