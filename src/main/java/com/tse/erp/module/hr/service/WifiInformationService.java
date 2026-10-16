package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.WifiInformation;

import java.util.List;

public interface WifiInformationService {

    List<WifiInformation> getAllWifiInformations(Long buId);

    WifiInformation getWifiInformationById(Long id);

    WifiInformation createWifiInformation(
            WifiInformation wifiInformation);

    WifiInformation updateWifiInformation(
            Long id, WifiInformation wifiInformation);

    void deleteWifiInformation(Long id);
}
