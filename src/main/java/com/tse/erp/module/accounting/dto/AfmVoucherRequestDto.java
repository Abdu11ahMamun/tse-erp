package com.tse.erp.module.accounting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVoucherRequestDto {

    private Long bgId;
    private Long buId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate voucherDate;

    private String voucherType;
    private String voucherNo;
    private Long locationId;
    private String paymentType;
    private String paymentTo;
    private String paymentThrough;
    private String periodName;
    private BigDecimal voucherAmount;
    private String voucherSource;
    private String voucherSourceLink;
    private String voucherStatus;
    private String approvalCode;
    private String bankAccount;
    private String chequeNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate chequeDate;

    private BigDecimal chequeAmount;
    private Integer isPosted;
    private Integer isReconciled;
    private String narration;
    private Integer status;

    private List<AfmVoucherDetailDto> voucherDetails;
    private List<AfmVoucherAttachDto> voucherAttachments;
}
