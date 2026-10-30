package com.tse.erp.module.admin.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id")
    private Long bgId;

    @Column(name = "bu_id")
    private Long buId;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "desig_id")
    private Long desigId;

    @Column(name = "user_name", length = 20)
    private String userName;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "mobile_no", length = 20)
    private String mobileNo;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @com.fasterxml.jackson.annotation.JsonProperty(
            access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password")
    private String password;


    @Column(name = "from_date")
    private LocalDateTime fromDate;

    @Column(name = "to_date")
    private LocalDateTime toDate;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "user_type_id")
    private Long userTypeId;

    @Column(name = "is_android_fixed", columnDefinition = "TINYINT")
    private Integer isAndroidFixed;

    @Column(name = "android_id")
    private String androidId;

    @Transient
    private Long employeeId;
}