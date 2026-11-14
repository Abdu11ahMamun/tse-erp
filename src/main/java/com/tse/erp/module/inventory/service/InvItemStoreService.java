package com.tse.erp.module.inventory.service;

import com.tse.erp.module.inventory.dto.InvItemStoreRequestDto;
import com.tse.erp.module.inventory.dto.InvItemStoreResponseDto;

import java.util.List;

public interface InvItemStoreService {

    List<InvItemStoreResponseDto> getAllItemStores(
            Long bgId, Long buId, String storeName, String storeCode, Integer isDefault, Integer status);

    InvItemStoreResponseDto getItemStoreById(Long id);

    String generateNextStoreCode(Long buId);

    InvItemStoreResponseDto createItemStore(InvItemStoreRequestDto request);

    InvItemStoreResponseDto updateItemStore(Long id, InvItemStoreRequestDto request);

    void deleteItemStore(Long id);
}

