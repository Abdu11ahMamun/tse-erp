package com.tse.erp.module.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequestDto {

    @NotNull(message = "Role id cannot be empty")
    private Long roleId;
}