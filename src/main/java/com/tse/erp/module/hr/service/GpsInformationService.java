package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.GpsInformation;

import java.util.List;

public interface GpsInformationService {

    List<GpsInformation> getAllGpsInformations(Long buId);

    GpsInformation getGpsInformationById(Long id);

    GpsInformation createGpsInformation(
            GpsInformation gpsInformation);

    GpsInformation updateGpsInformation(
            Long id, GpsInformation gpsInformation);

    void deleteGpsInformation(Long id);
}
