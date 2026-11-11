package com.tse.erp.module.inventory.service;

import com.tse.erp.module.inventory.dto.InvItemWarehouseRequestDto;
import com.tse.erp.module.inventory.dto.InvItemWarehouseResponseDto;

import java.util.List;

public interface InvItemWarehouseService {

    List<InvItemWarehouseResponseDto> getAllItemWarehouses(
            Long bgId, Long buId, String warehouseName, String warehouseCode, String storeId, Integer isDefault, Integer status);

    InvItemWarehouseResponseDto getItemWarehouseById(Long id);

    String generateNextWarehouseCode(Long buId);

    InvItemWarehouseResponseDto createItemWarehouse(InvItemWarehouseRequestDto request);

    InvItemWarehouseResponseDto updateItemWarehouse(Long id, InvItemWarehouseRequestDto request);

    void deleteItemWarehouse(Long id);
}

