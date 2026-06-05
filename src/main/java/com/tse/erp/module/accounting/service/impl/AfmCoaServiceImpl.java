package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.module.accounting.entity.AfmCoa;
import com.tse.erp.module.accounting.repository.AfmCoaRepository;
import com.tse.erp.module.accounting.service.AfmCoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AfmCoaServiceImpl implements AfmCoaService {

    private final AfmCoaRepository afmCoaRepository;

    @Override
    public List<AfmCoa> getAllCoas() {
        return afmCoaRepository.findAll();
    }

    @Override
    public AfmCoa getCoaById(Long id) {
        return afmCoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("COA not found with id: " + id));
    }

    @Override
    public AfmCoa createCoa(AfmCoa afmCoa) {
        return afmCoaRepository.save(afmCoa);
    }

    @Override
    public AfmCoa updateCoa(Long id, AfmCoa afmCoa) {
        // Agে check koro exist kore kina
        AfmCoa existing = getCoaById(id);

        // Update koro
        existing.setAccountCode(afmCoa.getAccountCode());
        existing.setAccountHead(afmCoa.getAccountHead());
        existing.setLedgerHeadType(afmCoa.getLedgerHeadType());
        existing.setAccountType(afmCoa.getAccountType());
        existing.setParentAccountHeadId(afmCoa.getParentAccountHeadId());
        existing.setAccountUsage(afmCoa.getAccountUsage());
        existing.setStatus(afmCoa.getStatus());

        return afmCoaRepository.save(existing);
    }

    @Override
    public void deleteCoa(Long id) {
        // Agে check koro exist kore kina
        AfmCoa existing = getCoaById(id);
        afmCoaRepository.delete(existing);
    }
}