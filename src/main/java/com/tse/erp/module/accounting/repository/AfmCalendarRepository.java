package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmCalendarRepository extends JpaRepository<AfmCalendar, Long> {

    List<AfmCalendar> findAllByOrderByIdDesc();

    List<AfmCalendar> findByBgIdOrderByIdDesc(Long bgId);

    List<AfmCalendar> findByBuIdOrderByIdDesc(Long buId);

    List<AfmCalendar> findByBgIdAndBuIdOrderByIdDesc(Long bgId, Long buId);

    List<AfmCalendar> findByBuIdAndStatusOrderByIdDesc(Long buId, Integer status);

    List<AfmCalendar> findByBgIdAndBuIdAndStatusOrderByIdDesc(Long bgId, Long buId, Integer status);

    List<AfmCalendar> findByBuIdAndCalendarYearIgnoreCase(Long buId, String calendarYear);

    boolean existsByBuIdAndCalendarYearIgnoreCase(Long buId, String calendarYear);

    @Query("SELECT DISTINCT c.calendarYear FROM AfmCalendar c WHERE c.buId = :buId AND (c.status IS NULL OR c.status = 1) ORDER BY c.calendarYear DESC")
    List<String> findDistinctCalendarYearsByBuId(@Param("buId") Long buId);
}
