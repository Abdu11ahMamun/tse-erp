package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmUserAccHeadMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AfmUserAccHeadMapRepository extends JpaRepository<AfmUserAccHeadMap, Long> {

    List<AfmUserAccHeadMap> findByBuIdAndUserIdOrderByIdAsc(Long buId, Long userId);

    List<AfmUserAccHeadMap> findByUserIdOrderByIdAsc(Long userId);

    List<AfmUserAccHeadMap> findByBuIdAndUserIdAndStatusOrderByIdAsc(Long buId, Long userId, Integer status);

    Optional<AfmUserAccHeadMap> findByBuIdAndUserIdAndAccountHeadId(Long buId, Long userId, Long accountHeadId);

    boolean existsByBuIdAndUserIdAndAccountHeadId(Long buId, Long userId, Long accountHeadId);
}
