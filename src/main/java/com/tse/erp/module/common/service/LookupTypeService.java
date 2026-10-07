package com.tse.erp.module.common.service;

import com.tse.erp.module.common.entity.LookupType;
import java.util.List;

public interface LookupTypeService {

    List<LookupType> getAllLookupTypes(Long buId);

    LookupType getLookupTypeById(Long id);

    LookupType createLookupType(LookupType lookupType);

    LookupType updateLookupType(Long id, LookupType lookupType);

    void deleteLookupType(Long id);
}