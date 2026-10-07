package com.tse.erp.module.common.repository;

import com.tse.erp.module.common.entity.LookupValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LookupValueRepository
        extends JpaRepository<LookupValue, Long> {

    List<LookupValue> findAllByOrderByIdDesc();

    List<LookupValue> findByBuIdOrderByIdDesc(Long buId);

    List<LookupValue> findByTypeIdOrderBySortOrderAscIdAsc(Long typeId);

    List<LookupValue> findByBuIdAndTypeIdOrderBySortOrderAscIdAsc(
            Long buId, Long typeId);

    // Duplicate check (Type-er moddhe unique)
    List<LookupValue> findByTypeIdAndValueIgnoreCase(
            Long typeId, String value);

    boolean existsByTypeId(Long typeId);
}