package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.entity.BusinessUnit;
import java.util.List;

public interface BusinessUnitService {

    List<BusinessUnit> getAllBusinessUnits();

    List<BusinessUnit> getBusinessUnitsByGroupId(Long bgId);

    BusinessUnit getBusinessUnitById(Long id);

    BusinessUnit createBusinessUnit(BusinessUnit businessUnit);

    BusinessUnit updateBusinessUnit(Long id,
                                    BusinessUnit businessUnit);

    void deleteBusinessUnit(Long id);
}