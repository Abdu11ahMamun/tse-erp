package com.tse.erp.module.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVTypePTypeMapRequestDto {

    private Long bgId;
    private Long buId;
    private Long voucherTypeId;
    private Long paymentTypeId;
    private Integer status;
}