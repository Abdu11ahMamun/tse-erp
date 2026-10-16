package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.OfficeNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficeNoticeRepository
        extends JpaRepository<OfficeNotice, Long> {

    List<OfficeNotice> findAllByOrderByIdDesc();

    List<OfficeNotice> findByBgIdOrderByIdDesc(Long bgId);
}