package com.tse.erp.module.inventory.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "INV_ITEM_WAREHS_EMPL_MAP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemWarehouseEmployeeMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "WAREHOUSE_ID", nullable = false)
    private Long warehouseId;

    @Column(name = "EMPLOYEE_ID", nullable = false)
    private Long employeeId;
}
