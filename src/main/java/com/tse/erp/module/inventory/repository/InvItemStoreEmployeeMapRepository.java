package com.tse.erp.module.inventory.repository;

import com.tse.erp.module.inventory.entity.InvItemStoreEmployeeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvItemStoreEmployeeMapRepository extends JpaRepository<InvItemStoreEmployeeMap, Long> {

    List<InvItemStoreEmployeeMap> findByStoreIdOrderByIdAsc(Long storeId);

    Optional<InvItemStoreEmployeeMap> findByStoreIdAndEmployeeId(Long storeId, Long employeeId);

    void deleteByStoreIdAndEmployeeId(Long storeId, Long employeeId);

    void deleteByStoreId(Long storeId);
}

