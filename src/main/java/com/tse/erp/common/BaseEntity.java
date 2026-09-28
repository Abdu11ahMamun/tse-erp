package com.tse.erp.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

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
        this.updatedDatetime = LocalDateTime.now();
        if (this.status == null) {
            this.status = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDatetime = LocalDateTime.now();
    }
}