package com.tse.erp.module.common.repository;

import com.tse.erp.module.common.entity.LookupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LookupTypeRepository
        extends JpaRepository<LookupType, Long> {

    List<LookupType> findAllByOrderByIdDesc();

    // BU diye filter
    List<LookupType> findByBuIdOrderByIdDesc(Long buId);

    // Duplicate check (BU-er moddhe unique)
    List<LookupType> findByBuIdAndTypeNameIgnoreCase(
            Long buId, String typeName);

    List<LookupType> findByBuIdAndTypeCodeIgnoreCase(
            Long buId, String typeCode);
}