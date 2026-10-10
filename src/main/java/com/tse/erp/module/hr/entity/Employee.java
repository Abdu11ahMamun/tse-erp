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

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "employee_name", nullable = false, length = 150)
    private String employeeName;

    @Column(name = "employee_code", length = 50, unique = true)
    private String employeeCode;

    @Column(name = "photo", length = 255)
    private String photo;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "desig_id", nullable = false)
    private Long desigId;

    @Column(name = "employment_type_id", nullable = false)
    private Long employmentTypeId;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "employment_status_id", nullable = false)
    private Long employmentStatusId;

    @Column(name = "probationary_period", nullable = false)
    private Integer probationaryPeriod;

    @Column(name = "notice_period", nullable = false)
    private Integer noticePeriod;

    @Column(name = "separation_date")
    private LocalDate separationDate;

    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "office_in_time", nullable = false)
    private LocalTime officeInTime;

    @Column(name = "office_out_time", nullable = false)
    private LocalTime officeOutTime;

    @Column(name = "overtime_allowance", nullable = false)
    private Boolean overtimeAllowance;

    @Column(name = "reporting_officer_id")
    private Long reportingOfficerId;

    @Column(name = "father_name", nullable = false, length = 150)
    private String fatherName;

    @Column(name = "mother_name", nullable = false, length = 150)
    private String motherName;

    @Column(name = "spouse_name", length = 150)
    private String spouseName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "birth_place", nullable = false, length = 150)
    private String birthPlace;

    @Column(name = "gender_id", nullable = false)
    private Long genderId;

    @Column(name = "marital_status", nullable = false, length = 20)
    private String maritalStatus;

    @Column(name = "date_of_marriage")
    private LocalDate dateOfMarriage;

    @Column(name = "religion_id", nullable = false)
    private Long religionId;

    @Column(name = "nationality_id", nullable = false)
    private Long nationalityId;

    @Column(name = "blood_group_id", nullable = false)
    private Long bloodGroupId;

    @Column(name = "highest_education_id", nullable = false)
    private Long highestEducationId;

    @Column(name = "nid", nullable = false, length = 20)
    private String nid;

    @Column(name = "passport_no", length = 20)
    private String passportNo;

    @Column(name = "driving_license_no", length = 30)
    private String drivingLicenseNo;

    @Column(name = "no_of_child", nullable = false)
    private Integer noOfChild;

    @Column(name = "male_child", nullable = false)
    private Integer maleChild;

    @Column(name = "female_child", nullable = false)
    private Integer femaleChild;

    @Column(name = "present_address", nullable = false, length = 500)
    private String presentAddress;

    @Column(name = "permanent_address", nullable = false, length = 500)
    private String permanentAddress;

    @Column(name = "mobile_no", nullable = false, length = 20)
    private String mobileNo;

    @Column(name = "whatsapp_no", length = 20)
    private String whatsappNo;

    @Column(name = "residence_phone", length = 20)
    private String residencePhone;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "fax", length = 30)
    private String fax;
}
