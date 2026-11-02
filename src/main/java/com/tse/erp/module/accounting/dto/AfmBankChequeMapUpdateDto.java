package com.tse.erp.module.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmBankChequeMapUpdateDto {

    private String chequeStatus;
    private Integer status;
}

