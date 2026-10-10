package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.DeptDesigMap;

import java.util.List;

public interface DeptDesigMapService {

    List<DeptDesigMap> getAllMappings(Long buId);

    DeptDesigMap getMappingById(Long id);

    DeptDesigMap createMapping(DeptDesigMap mapping);

    DeptDesigMap updateMapping(Long id, DeptDesigMap mapping);

    void deleteMapping(Long id);
}
