package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.OfficeCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficeCalendarRepository
        extends JpaRepository<OfficeCalendar, Long> {

    List<OfficeCalendar> findAllByOrderByIdDesc();

    List<OfficeCalendar> findByBuIdOrderByIdDesc(Long buId);

    List<OfficeCalendar> findByYearOrderByIdDesc(Integer year);

    List<OfficeCalendar> findByBuIdAndYearOrderByIdDesc(
            Long buId, Integer year);

    List<OfficeCalendar> findByBuIdAndYearAndMonthId(
            Long buId, Integer year, Long monthId);
}
