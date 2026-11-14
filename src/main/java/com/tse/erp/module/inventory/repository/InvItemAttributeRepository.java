package com.tse.erp.module.inventory.repository;

import com.tse.erp.module.inventory.entity.InvItemAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvItemAttributeRepository extends JpaRepository<InvItemAttribute, Long> {

    List<InvItemAttribute> findByBuIdOrderByAttributeNameAsc(Long buId);

    List<InvItemAttribute> findByBuIdAndStatusOrderByAttributeNameAsc(Long buId, Integer status);

    boolean existsByBuIdAndAttributeCodeIgnoreCase(Long buId, String attributeCode);

    boolean existsByBuIdAndAttributeNameIgnoreCase(Long buId, String attributeName);

    Optional<InvItemAttribute> findByBuIdAndAttributeCodeIgnoreCase(Long buId, String attributeCode);

    Optional<InvItemAttribute> findByBuIdAndAttributeNameIgnoreCase(Long buId, String attributeName);

    long countByBuId(Long buId);

    @Query("SELECT a FROM InvItemAttribute a WHERE " +
           "(:bgId IS NULL OR a.bgId = :bgId) AND " +
           "(:buId IS NULL OR a.buId = :buId) AND " +
           "(:attributeName IS NULL OR LOWER(a.attributeName) LIKE LOWER(CONCAT('%', :attributeName, '%'))) AND " +
           "(:attributeCode IS NULL OR LOWER(a.attributeCode) LIKE LOWER(CONCAT('%', :attributeCode, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) " +
           "ORDER BY a.id DESC")
    List<InvItemAttribute> searchItemAttributes(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("attributeName") String attributeName,
            @Param("attributeCode") String attributeCode,
            @Param("status") Integer status);
}

