package com.tse.erp.module.admin.repository;

import com.tse.erp.module.admin.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByOrderByIdDesc();

    // Module id diye filter
    List<Menu> findByModuleIdOrderByIdDesc(Long moduleId);

    // Duplicate check — same module e same menu name
    List<Menu> findByMenuNameIgnoreCaseAndModuleId(
            String menuName, Long moduleId);
}