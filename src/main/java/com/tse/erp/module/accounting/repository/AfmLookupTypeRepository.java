package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmLookupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AfmLookupTypeRepository extends JpaRepository<AfmLookupType, Long> {

    List<AfmLookupType> findAllByOrderByIdDesc();

    List<AfmLookupType> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmLookupType> findByBuIdOrderByIdDesc(Long buId);

    List<AfmLookupType> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmLookupType> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmLookupType> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);

    List<AfmLookupType> findByBuIdAndTypeNameIgnoreCase(Long buId, String typeName);

    List<AfmLookupType> findByBuIdAndTypeCodeIgnoreCase(Long buId, String typeCode);

    Optional<AfmLookupType> findByBuIdAndTypeCode(Long buId, String typeCode);

    boolean existsByBuIdAndTypeNameIgnoreCase(Long buId, String typeName);

    boolean existsByBuIdAndTypeCodeIgnoreCase(Long buId, String typeCode);

    List<AfmLookupType> findByTypeCodeIgnoreCase(String typeCode);

    Optional<AfmLookupType> findFirstByTypeCodeIgnoreCase(String typeCode);

    Optional<AfmLookupType> findFirstByBuIdAndTypeCodeIgnoreCase(Long buId, String typeCode);
}
