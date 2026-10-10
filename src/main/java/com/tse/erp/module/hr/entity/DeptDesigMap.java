package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hr_dept_desig_map")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeptDesigMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "desig_id", nullable = false)
    private Long desigId;
}
