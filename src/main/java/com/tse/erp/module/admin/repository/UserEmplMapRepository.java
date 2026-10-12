package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.UserEmplMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEmplMapRepository
        extends JpaRepository<UserEmplMap, Long> {

    List<UserEmplMap> findByUserIdOrderByIdAsc(Long userId);

    List<UserEmplMap> findByEmplIdAndStatus(Long emplId, Integer status);
}
