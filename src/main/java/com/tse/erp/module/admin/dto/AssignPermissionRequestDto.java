package com.tse.erp.module.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AssignPermissionRequestDto {

    @NotNull(message = "Module id cannot be empty")
    private Long moduleId;

    @NotNull(message = "Menu id cannot be empty")
    private Long menuId;

    @NotEmpty(message = "Permission list cannot be empty")
    private List<Long> permissionIds;
}