package com.tse.erp.module.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

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

    @Column(name = "doctors_id", columnDefinition = "LONGTEXT")
    private String doctorsId;

    @Column(name = "bu_id", columnDefinition = "LONGTEXT")
    private String buId;

    @Column(name = "is_active", columnDefinition = "BIT")
    private Integer isActive;

    @Column(name = "gender")
    private String gender;

    @Column(name = "ref_code")
    private String refCode;

    @Column(name = "verification_type")
    private String verificationType;

    @Column(name = "verification_no")
    private String verificationNo;

    @Column(name = "is_android_fixed", columnDefinition = "BIT")
    private Integer isAndroidFixed;

    @Column(name = "android_id")
    private String androidId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "remember_token")
    private String rememberToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}