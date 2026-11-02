package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmLookupValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmLookupValueRepository extends JpaRepository<AfmLookupValue, Long> {

    List<AfmLookupValue> findByTypeNameIdOrderBySortOrderAscIdAsc(Long typeNameId);

    List<AfmLookupValue> findByTypeNameIdAndStatusOrderBySortOrderAscIdAsc(Long typeNameId, Integer status);

    List<AfmLookupValue> findByBuIdAndTypeCodeOrderBySortOrderAscIdAsc(Long buId, String typeCode);

    List<AfmLookupValue> findByBuIdAndTypeCodeAndStatusOrderBySortOrderAscIdAsc(Long buId, String typeCode, Integer status);

    List<AfmLookupValue> findByBuIdOrderBySortOrderAscIdAsc(Long buId);

    List<AfmLookupValue> findByBgIdAndBuIdOrderBySortOrderAscIdAsc(Long bgId, Long buId);

    List<AfmLookupValue> findByTypeNameIdAndValueIgnoreCase(Long typeNameId, String value);

    List<AfmLookupValue> findByTypeNameIdAndValueCodeIgnoreCase(Long typeNameId, String valueCode);

    boolean existsByTypeNameId(Long typeNameId);

    void deleteByTypeNameId(Long typeNameId);

    List<AfmLookupValue> findByTypeCodeIgnoreCaseOrderBySortOrderAscIdAsc(String typeCode);

    List<AfmLookupValue> findByTypeCodeIgnoreCaseAndStatusOrderBySortOrderAscIdAsc(String typeCode, Integer status);

    List<AfmLookupValue> findByBuIdAndTypeCodeIgnoreCaseOrderBySortOrderAscIdAsc(Long buId, String typeCode);

    List<AfmLookupValue> findByBuIdAndTypeCodeIgnoreCaseAndStatusOrderBySortOrderAscIdAsc(Long buId, String typeCode, Integer status);

    @Query("SELECT v FROM AfmLookupValue v " +
           "WHERE (LOWER(v.typeCode) = LOWER(:typeCode) " +
           "   OR v.typeNameId IN (SELECT t.id FROM AfmLookupType t WHERE LOWER(t.typeCode) = LOWER(:typeCode) OR LOWER(t.typeName) = LOWER(:typeCode))) " +
           "  AND (:buId IS NULL OR v.buId = :buId) " +
           "  AND (:status IS NULL OR v.status = :status) " +
           "ORDER BY v.sortOrder ASC, v.id ASC")
    List<AfmLookupValue> findValuesByTypeCode(
            @Param("typeCode") String typeCode,
            @Param("buId") Long buId,
            @Param("status") Integer status);
}
