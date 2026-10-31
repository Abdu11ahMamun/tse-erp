package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "AFM_VOUCHER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVoucher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "VOUCHER_DATE")
    private LocalDate voucherDate;

    @Column(name = "VOUCHER_TYPE", length = 50)
    private String voucherType;

    @Column(name = "VOUCHER_NO", length = 50)
    private String voucherNo;

    @Column(name = "LOCATION_ID")
    private Long locationId;

    @Column(name = "PAYMENT_TYPE", length = 50)
    private String paymentType;

    @Column(name = "PAYMENT_TO", length = 200)
    private String paymentTo;

    @Column(name = "PAYMENT_THROUGH", length = 50)
    private String paymentThrough;

    @Column(name = "PERIOD_NAME", length = 50)
    private String periodName;

    @Column(name = "VOUCHER_AMOUNT", precision = 18, scale = 2)
    private BigDecimal voucherAmount;

    @Column(name = "VOUCHER_SOURCE", length = 50)
    private String voucherSource;

    @Column(name = "VOUCHER_SOURCE_LINK", length = 250)
    private String voucherSourceLink;

    @Column(name = "VOUCHER_STATUS", length = 50)
    private String voucherStatus;

    @Column(name = "APPROVAL_CODE", length = 50)
    private String approvalCode;

    @Column(name = "BANK_ACCOUNT", length = 100)
    private String bankAccount;

    @Column(name = "CHEQUE_NO", length = 50)
    private String chequeNo;

    @Column(name = "CHEQUE_DATE")
    private LocalDate chequeDate;

    @Column(name = "CHEQUE_AMOUNT", precision = 18, scale = 2)
    private BigDecimal chequeAmount;

    @Column(name = "IS_POSTED")
    private Integer isPosted;

    @Column(name = "IS_RECONCILED")
    private Integer isReconciled;

    @Column(name = "NARRATION", length = 1000)
    private String narration;
}
