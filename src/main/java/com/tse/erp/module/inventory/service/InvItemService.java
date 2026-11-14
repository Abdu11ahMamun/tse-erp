package com.tse.erp.module.inventory.service;

import com.tse.erp.module.inventory.dto.InvItemAttributeDtlsDto;
import com.tse.erp.module.inventory.dto.InvItemRequestDto;
import com.tse.erp.module.inventory.dto.InvItemResponseDto;

import java.util.List;

public interface InvItemService {

    List<InvItemResponseDto> getAllItems(
            Long bgId, Long buId, Long categoryId, Long brandId, Long uomId, String storeId, String itemName, String itemCode, Integer status);

    InvItemResponseDto getItemById(Long id);

    String generateNextItemCode(Long buId);

    InvItemResponseDto createItem(InvItemRequestDto request);

    InvItemResponseDto updateItem(Long id, InvItemRequestDto request);

    void deleteItem(Long id);

    List<InvItemAttributeDtlsDto> getItemDetails(Long itemId);

    InvItemAttributeDtlsDto addItemDetail(Long itemId, InvItemAttributeDtlsDto detailDto);

    InvItemAttributeDtlsDto updateItemDetail(Long itemId, Long detailId, InvItemAttributeDtlsDto detailDto);

    void deleteItemDetail(Long itemId, Long detailId);
}

