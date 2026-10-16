package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.OfficeNotice;

import java.util.List;

public interface OfficeNoticeService {

    List<OfficeNotice> getAllOfficeNotices(Long bgId, boolean current);

    OfficeNotice getOfficeNoticeById(Long id);

    OfficeNotice createOfficeNotice(OfficeNotice officeNotice);

    OfficeNotice updateOfficeNotice(Long id, OfficeNotice officeNotice);

    void deleteOfficeNotice(Long id);
}