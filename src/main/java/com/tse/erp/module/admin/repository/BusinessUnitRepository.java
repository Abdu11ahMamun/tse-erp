package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.BusinessUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BusinessUnitRepository
        extends JpaRepository<BusinessUnit, Long> {

    List<BusinessUnit> findAllByOrderByIdDesc();

    // Unit name duplicate check
    List<BusinessUnit> findByBusinessUnitIgnoreCase(
            String businessUnit);

    // BG id diye filter
    List<BusinessUnit> findByBgIdOrderByIdDesc(Long bgId);
}