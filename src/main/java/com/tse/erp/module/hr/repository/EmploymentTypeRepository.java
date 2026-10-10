package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmploymentTypeRepository
        extends JpaRepository<EmploymentType, Long> {

    List<EmploymentType> findAllByOrderBySortOrderAscIdAsc();

    List<EmploymentType> findByTypeNameIgnoreCase(String typeName);

    List<EmploymentType> findByShortNameIgnoreCase(String shortName);
}
