package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "afm_ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmLedger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "voucher_date")
    private LocalDate voucherDate;

    @Column(name = "voucher_type_id")
    private Long voucherTypeId;

    @Column(name = "voucher_type", length = 50)
    private String voucherType;

    @Column(name = "voucher_no", length = 50)
    private String voucherNo;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "payment_type_id")
    private Long paymentTypeId;

    @Column(name = "payment_type", length = 50)
    private String paymentType;

    @Column(name = "payment_to", length = 200)
    private String paymentTo;

    @Column(name = "payment_through_id")
    private Long paymentThroughId;

    @Column(name = "payment_through", length = 50)
    private String paymentThrough;

    @Column(name = "period_name", length = 50)
    private String periodName;

    @Column(name = "voucher_amount", precision = 18, scale = 2)
    private BigDecimal voucherAmount;

    @Column(name = "voucher_source", length = 50)
    private String voucherSource;

    @Column(name = "voucher_source_link", length = 250)
    private String voucherSourceLink;

    @Column(name = "voucher_status", length = 50)
    private String voucherStatus;

    @Column(name = "approval_code", length = 50)
    private String approvalCode;

    @Column(name = "bank_account", length = 100)
    private String bankAccount;

    @Column(name = "cheque_no", length = 50)
    private String chequeNo;

    @Column(name = "cheque_date")
    private LocalDate chequeDate;

    @Column(name = "cheque_amount", precision = 18, scale = 2)
    private BigDecimal chequeAmount;

    @Column(name = "is_posted")
    private Integer isPosted;

    @Column(name = "is_reconciled")
    private Integer isReconciled;

    @Column(name = "narration", length = 1000)
    private String narration;
}
