package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.UserTypeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTypeGroupRepository
        extends JpaRepository<UserTypeGroup, Long> {

    List<UserTypeGroup> findByUserTypeIdOrderByIdAsc(Long userTypeId);

    List<UserTypeGroup> findByBuIdOrderByIdDesc(Long buId);

    List<UserTypeGroup> findByUserTypeIdAndBuId(
            Long userTypeId, Long buId);

    List<UserTypeGroup> findAllByOrderByIdDesc();

    boolean existsByUserTypeId(Long userTypeId);

    boolean existsByBuId(Long buId);
}
