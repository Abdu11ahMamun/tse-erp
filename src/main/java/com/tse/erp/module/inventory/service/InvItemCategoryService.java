package com.tse.erp.module.inventory.service;

import com.tse.erp.module.inventory.dto.InvItemCategoryRequestDto;
import com.tse.erp.module.inventory.dto.InvItemCategoryResponseDto;

import java.util.List;

public interface InvItemCategoryService {

    List<InvItemCategoryResponseDto> getAllItemCategories(Long bgId, Long buId, String categoryName, String categoryCode, Integer status);

    InvItemCategoryResponseDto getItemCategoryById(Long id);

    String generateNextCategoryCode(Long buId);

    InvItemCategoryResponseDto createItemCategory(InvItemCategoryRequestDto request);

    InvItemCategoryResponseDto updateItemCategory(Long id, InvItemCategoryRequestDto request);

    void deleteItemCategory(Long id);
}

