package com.tse.erp.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public abstract class BaseEntity {

    public static final String APP_NAME = AppConstants.APP_NAME;

    @Column(name = "created_user_id")
    private Long createdUserId;

    @Column(name = "created_emp_id")
    private Long createdEmpId;

    @Column(name = "created_datetime")
    private LocalDateTime createdDatetime;

    @Column(name = "updated_user_id")
    private Long updatedUserId;

    @Column(name = "updated_emp_id")
    private Long updatedEmpId;

    @Column(name = "updated_datetime")
    private LocalDateTime updatedDatetime;

    @Column(name = "created_app")
    private String createdApp;

    @Column(name = "updated_app")
    private String updatedApp;

    @Column(name = "status", columnDefinition = "TINYINT")
    private Integer status;

    @PrePersist
    protected void onCreate() {
        this.createdDatetime = LocalDateTime.now();
        if (this.createdApp == null) {
            this.createdApp = AppConstants.APP_NAME;
        }
        if (this.status == null) {
            this.status = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDatetime = LocalDateTime.now();
        this.updatedApp = AppConstants.APP_NAME;
    }
}