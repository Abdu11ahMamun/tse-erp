package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    // Duplicate check er jonno
    List<Module> findByModuleNameIgnoreCase(String moduleName);
}
