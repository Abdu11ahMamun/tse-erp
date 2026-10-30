package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmCostCenterRequestDto;
import com.tse.erp.module.accounting.dto.AfmCostCenterResponseDto;

import java.util.List;

public interface AfmCostCenterService {

    List<AfmCostCenterResponseDto> getAllCostCenters(
            Long bgId,
            Long buId,
            Long projectId,
            Long departmentId,
            Integer status
    );

    AfmCostCenterResponseDto getCostCenterById(Long id);

    AfmCostCenterResponseDto createCostCenter(AfmCostCenterRequestDto request);

    AfmCostCenterResponseDto updateCostCenter(Long id, AfmCostCenterRequestDto request);

    void deleteCostCenter(Long id);
}

