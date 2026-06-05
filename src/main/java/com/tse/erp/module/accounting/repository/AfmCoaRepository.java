package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmCoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AfmCoaRepository extends JpaRepository<AfmCoa, Long> {

}