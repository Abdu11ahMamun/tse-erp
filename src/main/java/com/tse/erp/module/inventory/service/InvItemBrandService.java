package com.tse.erp.module.inventory.service;

import com.tse.erp.module.inventory.dto.InvItemBrandRequestDto;
import com.tse.erp.module.inventory.dto.InvItemBrandResponseDto;

import java.util.List;

public interface InvItemBrandService {

    List<InvItemBrandResponseDto> getAllItemBrands(Long bgId, Long buId, String brandName, String brandCode, Integer status);

    InvItemBrandResponseDto getItemBrandById(Long id);

    String generateNextBrandCode(Long buId);

    InvItemBrandResponseDto createItemBrand(InvItemBrandRequestDto request);

    InvItemBrandResponseDto updateItemBrand(Long id, InvItemBrandRequestDto request);

    void deleteItemBrand(Long id);
}

