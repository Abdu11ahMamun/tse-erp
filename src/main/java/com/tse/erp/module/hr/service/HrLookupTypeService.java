package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.HrLookupType;

import java.util.List;

public interface HrLookupTypeService {

    List<HrLookupType> getAllHrLookupTypes();

    HrLookupType getHrLookupTypeById(Long id);

    HrLookupType createHrLookupType(HrLookupType hrLookupType);

    HrLookupType updateHrLookupType(Long id, HrLookupType hrLookupType);

    void deleteHrLookupType(Long id);
}
