package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmSupplierInfoRequestDto;
import com.tse.erp.module.accounting.dto.AfmSupplierInfoResponseDto;

import java.util.List;

public interface AfmSupplierInfoService {

    List<AfmSupplierInfoResponseDto> getAllSuppliers(
            Long bgId,
            Long buId,
            String query,
            String supplierType,
            Integer status
    );

    AfmSupplierInfoResponseDto getSupplierById(Long id);

    AfmSupplierInfoResponseDto createSupplier(AfmSupplierInfoRequestDto request);

    AfmSupplierInfoResponseDto updateSupplier(Long id, AfmSupplierInfoRequestDto request);

    void deleteSupplier(Long id);

    String generateNextSupplierCode(Long buId);
}

