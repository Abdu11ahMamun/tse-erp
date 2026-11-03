package com.tse.erp.module.asset.service;

import com.tse.erp.module.asset.dto.GatePassDto;

import java.util.List;

public interface GatePassService {

    List<GatePassDto> getAll(Long buId);

    GatePassDto getById(Long id);

    GatePassDto create(GatePassDto dto);

    GatePassDto update(Long id, GatePassDto dto);

    void delete(Long id);
}