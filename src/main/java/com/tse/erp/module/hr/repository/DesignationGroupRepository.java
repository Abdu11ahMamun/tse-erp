package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.DesignationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignationGroupRepository
        extends JpaRepository<DesignationGroup, Long> {

    List<DesignationGroup> findAllByOrderByIdDesc();

    List<DesignationGroup> findByDesigIdOrderByIdDesc(Long desigId);

    List<DesignationGroup> findByDesigIdAndBuId(Long desigId, Long buId);

    boolean existsByDesigId(Long desigId);
}
