package com.tse.erp.common;

import com.tse.erp.security.CurrentUser;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class AuditEntityListener {

    @PrePersist
    public void onCreate(Object entity) {
        if (entity instanceof BaseEntity b) {
            if (b.getCreatedApp() == null) {
                b.setCreatedApp(AppConstants.APP_NAME);
            }
            CurrentUser.get().ifPresent(u -> {
                b.setCreatedUserId(u.userId());
                b.setCreatedEmpId(u.empId());
            });
        }
    }

    @PreUpdate
    public void onUpdate(Object entity) {
        if (entity instanceof BaseEntity b) {
            b.setUpdatedApp(AppConstants.APP_NAME);
            CurrentUser.get().ifPresent(u -> {
                b.setUpdatedUserId(u.userId());
                b.setUpdatedEmpId(u.empId());
            });
        }
    }
}