package com.tse.erp.module.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmUserSetupRequestDto {

    private Long bgId;
    private Long buId;
    private Long userId;

    private String voucherDate;
    private String voucherType;
    private String voucherTypeDefault;
    private String location;
    private String locationDefault;
    private String bankAccount;
    private String bankAccountDefault;
    private String costCenter;
    private String costCenterDefault;

    private Integer status;
}

