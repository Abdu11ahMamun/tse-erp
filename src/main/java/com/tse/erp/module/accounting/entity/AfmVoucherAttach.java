package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "afm_voucher_attach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVoucherAttach extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id")
    private Long bgId;

    @Column(name = "bu_id")
    private Long buId;

    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    @Column(name = "attachment_file_name", length = 250)
    private String attachmentFileName;

    @Lob
    @Column(name = "attachment_file", columnDefinition = "LONGTEXT")
    private String attachmentFile;

    @Column(name = "is_active")
    private Integer isActive;
}
