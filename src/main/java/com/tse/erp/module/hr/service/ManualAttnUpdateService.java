package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.ManualAttnUpdate;

import java.time.LocalDate;
import java.util.List;

public interface ManualAttnUpdateService {

    List<ManualAttnUpdate> getAll(Long buId, LocalDate attenDate);

    ManualAttnUpdate getById(Long id);

    ManualAttnUpdate create(ManualAttnUpdate r);

    ManualAttnUpdate update(Long id, ManualAttnUpdate r);

    void delete(Long id);
}