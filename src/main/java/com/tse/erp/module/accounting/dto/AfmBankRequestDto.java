package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmBankRequestDto {

    private Long bgId;
    private Long buId;
    private String bankName;
    private String shortName;
    private Integer status;

    private List<AfmBankDtlsDto> bankDetails;
}
