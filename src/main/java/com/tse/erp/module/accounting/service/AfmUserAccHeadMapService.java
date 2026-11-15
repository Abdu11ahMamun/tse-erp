package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmUserAccHeadMapDto;

import java.util.List;

public interface AfmUserAccHeadMapService {

    List<AfmUserAccHeadMapDto> getByUserId(Long userId, Long buId);

    AfmUserAccHeadMapDto create(AfmUserAccHeadMapDto dto);

    AfmUserAccHeadMapDto update(Long id, AfmUserAccHeadMapDto dto);

    List<AfmUserAccHeadMapDto> saveAllForUser(Long bgId, Long buId, Long userId, List<AfmUserAccHeadMapDto> items);
}
