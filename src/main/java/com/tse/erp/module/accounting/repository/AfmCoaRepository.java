package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmCoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmCoaRepository extends JpaRepository<AfmCoa, Long> {
    List<AfmCoa> findAllByOrderByIdDesc();
}