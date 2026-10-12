package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.UserBuMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBuMapRepository
        extends JpaRepository<UserBuMap, Long> {

    List<UserBuMap> findByUserIdOrderByIdAsc(Long userId);

    boolean existsByBuId(Long buId);
}
