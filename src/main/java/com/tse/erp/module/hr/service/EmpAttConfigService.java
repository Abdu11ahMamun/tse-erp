package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.dto.EmpAttConfigDto;

import java.util.List;

public interface EmpAttConfigService {

    List<EmpAttConfigDto> getAll(Long buId);

    EmpAttConfigDto getById(Long id);

    EmpAttConfigDto create(EmpAttConfigDto dto);

    EmpAttConfigDto update(Long id, EmpAttConfigDto dto);

    void delete(Long id);
}