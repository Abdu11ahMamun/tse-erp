package com.tse.erp.module.inventory.repository;

import com.tse.erp.module.inventory.entity.InvItemWarehouseEmployeeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvItemWarehouseEmployeeMapRepository extends JpaRepository<InvItemWarehouseEmployeeMap, Long> {

    List<InvItemWarehouseEmployeeMap> findByWarehouseIdOrderByIdAsc(Long warehouseId);

    List<InvItemWarehouseEmployeeMap> findByWarehouseIdAndStatusOrderByIdAsc(Long warehouseId, Integer status);

    Optional<InvItemWarehouseEmployeeMap> findByWarehouseIdAndEmployeeId(Long warehouseId, Long employeeId);

    boolean existsByWarehouseIdAndEmployeeId(Long warehouseId, Long employeeId);

    void deleteByWarehouseId(Long warehouseId);

    void deleteByWarehouseIdAndEmployeeId(Long warehouseId, Long employeeId);

    @Query("SELECT m FROM InvItemWarehouseEmployeeMap m WHERE " +
           "(:bgId IS NULL OR m.bgId = :bgId) AND " +
           "(:buId IS NULL OR m.buId = :buId) AND " +
           "(:warehouseId IS NULL OR m.warehouseId = :warehouseId) AND " +
           "(:employeeId IS NULL OR m.employeeId = :employeeId) AND " +
           "(:status IS NULL OR m.status = :status) " +
           "ORDER BY m.id DESC")
    List<InvItemWarehouseEmployeeMap> searchMaps(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("warehouseId") Long warehouseId,
            @Param("employeeId") Long employeeId,
            @Param("status") Integer status);
}
