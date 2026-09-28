package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.entity.BusinessGroup;
import java.util.List;

public interface BusinessGroupService {

    List<BusinessGroup> getAllBusinessGroups();

    BusinessGroup getBusinessGroupById(Long id);

    BusinessGroup createBusinessGroup(BusinessGroup businessGroup);

    BusinessGroup updateBusinessGroup(Long id,
                                      BusinessGroup businessGroup);

    void deleteBusinessGroup(Long id);
}