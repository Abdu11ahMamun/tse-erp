package com.tse.erp.module.common.service;

import com.tse.erp.module.common.entity.LookupValue;
import java.util.List;

public interface LookupValueService {

    List<LookupValue> getAllLookupValues(Long buId, Long typeId);

    LookupValue getLookupValueById(Long id);

    LookupValue createLookupValue(LookupValue lookupValue);

    LookupValue updateLookupValue(Long id, LookupValue lookupValue);

    void deleteLookupValue(Long id);
}