package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmCalendarMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfmCalendarMonthRepository extends JpaRepository<AfmCalendarMonth, Long> {

    List<AfmCalendarMonth> findByAfmCalendarIdOrderBySerialNoAsc(Long afmCalendarId);

    List<AfmCalendarMonth> findByAfmCalendarIdOrderByIdAsc(Long afmCalendarId);

    void deleteByAfmCalendarId(Long afmCalendarId);

    @Query("SELECT DISTINCT m.periodName FROM AfmCalendarMonth m, AfmCalendar c " +
           "WHERE m.afmCalendarId = c.id " +
           "  AND c.buId = :buId " +
           "  AND (m.calendarYear = :calendarYear OR c.calendarYear = :calendarYear) " +
           "  AND LOWER(m.periodStatus) = 'open' " +
           "  AND (m.status IS NULL OR m.status = 1) " +
           "ORDER BY m.periodName ASC")
    List<String> findOpenPeriodNamesByBuIdAndYear(@Param("buId") Long buId, @Param("calendarYear") String calendarYear);

    @Query("SELECT DISTINCT m.calendarYear FROM AfmCalendarMonth m, AfmCalendar c " +
           "WHERE m.afmCalendarId = c.id " +
           "  AND c.buId = :buId " +
           "  AND m.calendarYear IS NOT NULL " +
           "  AND TRIM(m.calendarYear) != '' " +
           "ORDER BY m.calendarYear ASC")
    List<String> findDistinctCalendarYearsByBuId(@Param("buId") Long buId);
}
