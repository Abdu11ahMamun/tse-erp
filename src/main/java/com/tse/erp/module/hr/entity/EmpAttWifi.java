package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hr_emp_att_wifi")
@Getter
@Setter
@NoArgsConstructor
public class EmpAttWifi extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "att_config_id", nullable = false)
    private Long attConfigId;

    @Column(name = "wifi_id", nullable = false)
    private Long wifiId;
}