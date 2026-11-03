package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVoucherDetailDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private Long voucherId;
    private Long coaId;
    private String accountCode;
    private String accountHeadName;
    private String coaSourceType;
    private String coaSourceName;
    private Long coaSourceId;
    private Long costCenterId;
    private String costCenterName;
    private BigDecimal drAmount;
    private BigDecimal crAmount;
    private Integer status;
}
