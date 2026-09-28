package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.BusinessGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BusinessGroupRepository
        extends JpaRepository<BusinessGroup, Long> {

    List<BusinessGroup> findAllByOrderByIdDesc();

    List<BusinessGroup> findByGroupNameIgnoreCase(String groupName);
}