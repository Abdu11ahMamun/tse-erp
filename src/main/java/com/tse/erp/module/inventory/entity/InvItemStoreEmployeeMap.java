package com.tse.erp.module.inventory.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "INV_ITEM_STORE_EMPL_MAP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemStoreEmployeeMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "STORE_ID", nullable = false)
    private Long storeId;

    @Column(name = "EMPLOYEE_ID", nullable = false)
    private Long employeeId;
}

