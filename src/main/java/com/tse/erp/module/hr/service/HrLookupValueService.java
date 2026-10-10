package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.HrLookupValue;

import java.util.List;

public interface HrLookupValueService {

    List<HrLookupValue> getAllHrLookupValues(Long bgId, Long typeId);

    HrLookupValue getHrLookupValueById(Long id);

    HrLookupValue createHrLookupValue(HrLookupValue hrLookupValue);

    HrLookupValue updateHrLookupValue(Long id, HrLookupValue hrLookupValue);

    void deleteHrLookupValue(Long id);
}
