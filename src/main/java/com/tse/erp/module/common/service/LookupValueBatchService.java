package com.tse.erp.module.common.service;

import com.tse.erp.module.common.dto.LookupValueBatchDto;

public interface LookupValueBatchService {

    LookupValueBatchDto getByType(Long typeId);

    LookupValueBatchDto saveByType(Long typeId, LookupValueBatchDto dto);
}
