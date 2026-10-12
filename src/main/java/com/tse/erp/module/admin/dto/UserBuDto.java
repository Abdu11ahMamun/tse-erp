package com.tse.erp.module.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserBuDto {

    private Long userId;
    private Long bgId;
    private List<UserBuRowDto> businessUnits;
}
