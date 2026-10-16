package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.OfficeCalendarDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficeCalendarDtlRepository
        extends JpaRepository<OfficeCalendarDtl, Long> {

    List<OfficeCalendarDtl>
    findByOfficeCalendarIdAndStatusOrderByCalendarDateAsc(
            Long officeCalendarId, Integer status);

    List<OfficeCalendarDtl>
    findByOfficeCalendarIdOrderByCalendarDateAsc(Long officeCalendarId);
}
