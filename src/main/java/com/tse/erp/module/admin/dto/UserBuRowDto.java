package com.tse.erp.module.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserBuRowDto {

    private Long id;
    private Long buId;
    private Integer status;
}
