package com.tse.erp.module.accounting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVoucherResponseDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private String businessGroupName;
    private String businessUnitName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate voucherDate;

    private Long voucherTypeId;
    private String voucherTypeName;
    private String voucherType;
    private String voucherNo;
    private Long locationId;
    private Long paymentTypeId;
    private String paymentTypeName;
    private String paymentType;
    private String paymentTo;
    private Long paymentThroughId;
    private String paymentThroughName;
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

    private Long createdUserId;
    private Long createdEmpId;
    private LocalDateTime createdDatetime;
    private Long updatedUserId;
    private Long updatedEmpId;
    private LocalDateTime updatedDatetime;
    private String createdApp;
    private String updatedApp;

    private List<AfmVoucherDetailDto> voucherDetails;
    private List<AfmVoucherAttachDto> voucherAttachments;
}
