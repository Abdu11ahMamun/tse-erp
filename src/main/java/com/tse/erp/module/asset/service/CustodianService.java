package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.dto.CustodianRequestDto;
import com.tse.erp.module.asset.dto.CustodianResponseDto;

import java.util.List;

public interface CustodianService {

    List<CustodianResponseDto> getAllCustodians(Long buId);

    CustodianResponseDto getCustodianById(Long id);

    CustodianResponseDto createCustodian(CustodianRequestDto request);

    CustodianResponseDto updateCustodian(
            Long id, CustodianRequestDto request);

    void deleteCustodian(Long id);
}
