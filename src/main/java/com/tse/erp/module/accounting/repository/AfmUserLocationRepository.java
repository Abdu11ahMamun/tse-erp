package com.tse.erp.module.accounting.repository;

import com.tse.erp.module.accounting.entity.AfmUserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AfmUserLocationRepository extends JpaRepository<AfmUserLocation, Long> {

    List<AfmUserLocation> findByBuIdOrderByLocationAsc(Long buId);

    List<AfmUserLocation> findByBuIdAndStatusOrderByLocationAsc(Long buId, Integer status);

    boolean existsByBuIdAndLocationIgnoreCase(Long buId, String location);

    Optional<AfmUserLocation> findByBuIdAndLocationIgnoreCase(Long buId, String location);

    @Query("SELECT u FROM AfmUserLocation u WHERE " +
           "(:bgId IS NULL OR u.bgId = :bgId) AND " +
           "(:buId IS NULL OR u.buId = :buId) AND " +
           "(:location IS NULL OR LOWER(u.location) LIKE LOWER(CONCAT('%', :location, '%')) OR LOWER(u.shortName) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:status IS NULL OR u.status = :status) " +
           "ORDER BY u.id DESC")
    List<AfmUserLocation> searchUserLocations(
            @Param("bgId") Long bgId,
            @Param("buId") Long buId,
            @Param("location") String location,
            @Param("status") Integer status);
}

