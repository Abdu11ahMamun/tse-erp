package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserTypeRepository extends JpaRepository<UserType, Long> {
    List<UserType> findAllByOrderByIdDesc();
    List<UserType> findByUserTypeIgnoreCase(String userType);
}