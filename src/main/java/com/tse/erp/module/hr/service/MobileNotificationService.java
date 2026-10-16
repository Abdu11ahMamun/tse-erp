package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.dto.MobileNotificationCreateDto;
import com.tse.erp.module.hr.entity.MobileNotification;

import java.util.List;

public interface MobileNotificationService {

    List<MobileNotification> getAllMobileNotifications(
            Long buId, Long emplId);

    MobileNotification getMobileNotificationById(Long id);

    List<MobileNotification> createMobileNotifications(
            MobileNotificationCreateDto dto);

    MobileNotification updateMobileNotification(
            Long id, MobileNotification r);

    void deleteMobileNotification(Long id);
}
