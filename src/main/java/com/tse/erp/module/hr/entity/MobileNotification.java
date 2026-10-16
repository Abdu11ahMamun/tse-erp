package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hr_mobile_notification")
@Getter
@Setter
@NoArgsConstructor
public class MobileNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "empl_id", nullable = false)
    private Long emplId;

    @Column(name = "notification_type", nullable = false)
    private Long notificationTypeId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "body", nullable = false, length = 1000)
    private String body;
}
