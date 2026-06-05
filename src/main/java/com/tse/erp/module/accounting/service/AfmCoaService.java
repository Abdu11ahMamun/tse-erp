package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.entity.AfmCoa;
import java.util.List;

public interface AfmCoaService {

    List<AfmCoa> getAllCoas();

    AfmCoa getCoaById(Long id);

    AfmCoa createCoa(AfmCoa afmCoa);

    AfmCoa updateCoa(Long id, AfmCoa afmCoa);

    void deleteCoa(Long id);
}