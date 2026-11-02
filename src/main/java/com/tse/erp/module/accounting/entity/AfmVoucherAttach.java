package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AFM_VOUCHER_ATTACH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVoucherAttach extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID")
    private Long bgId;

    @Column(name = "BU_ID")
    private Long buId;

    @Column(name = "VOUCHER_ID", nullable = false)
    private Long voucherId;

    @Column(name = "ATTACHMENT_FILE_NAME", length = 250)
    private String attachmentFileName;

    @Lob
    @Column(name = "ATTACHMENT_FILE", columnDefinition = "LONGTEXT")
    private String attachmentFile;

    @Column(name = "IS_ACTIVE")
    private Integer isActive;
}
