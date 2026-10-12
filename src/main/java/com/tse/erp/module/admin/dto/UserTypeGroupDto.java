package com.tse.erp.module.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserTypeGroupDto {

    private Long userTypeId;
    private Long bgId;
    private List<UserTypeGroupRowDto> businessUnits;
}
