package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.HrLookupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrLookupTypeRepository
        extends JpaRepository<HrLookupType, Long> {

    List<HrLookupType> findAllByOrderByIdDesc();

    List<HrLookupType> findByTypeNameIgnoreCase(String typeName);

    List<HrLookupType> findByCodeIgnoreCase(String code);
}
