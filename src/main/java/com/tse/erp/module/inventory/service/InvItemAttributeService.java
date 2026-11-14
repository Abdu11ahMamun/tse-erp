package com.tse.erp.module.inventory.service;

import com.tse.erp.module.inventory.dto.InvItemAttributeRequestDto;
import com.tse.erp.module.inventory.dto.InvItemAttributeResponseDto;

import java.util.List;

public interface InvItemAttributeService {

    List<InvItemAttributeResponseDto> getAllItemAttributes(Long bgId, Long buId, String attributeName, String attributeCode, Integer status);

    InvItemAttributeResponseDto getItemAttributeById(Long id);

    String generateNextAttributeCode(Long buId);

    InvItemAttributeResponseDto createItemAttribute(InvItemAttributeRequestDto request);

    InvItemAttributeResponseDto updateItemAttribute(Long id, InvItemAttributeRequestDto request);

    void deleteItemAttribute(Long id);
}

