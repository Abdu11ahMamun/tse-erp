package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.HrLookupValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrLookupValueRepository
        extends JpaRepository<HrLookupValue, Long> {

    List<HrLookupValue> findAllByOrderByIdDesc();

    List<HrLookupValue> findByBgIdOrderByIdDesc(Long bgId);

    List<HrLookupValue> findByTypeIdOrderBySortOrderAscIdAsc(Long typeId);

    List<HrLookupValue> findByBgIdAndTypeIdOrderBySortOrderAscIdAsc(
            Long bgId, Long typeId);

    List<HrLookupValue> findByBgIdAndTypeIdAndValueIgnoreCase(
            Long bgId, Long typeId, String value);

    boolean existsByTypeId(Long typeId);
}
