package com.tse.erp.module.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmUserLocationRequestDto {

    private Long bgId;
    private Long buId;
    private String location;
    private String shortName;
    private Integer status;
}

