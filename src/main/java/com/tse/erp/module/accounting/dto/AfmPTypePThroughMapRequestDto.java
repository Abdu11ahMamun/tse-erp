package com.tse.erp.module.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmPTypePThroughMapRequestDto {

    private Long bgId;
    private Long buId;
    private Long paymentTypeId;
    private Long paymentThroughId;
    private Integer status;
}
