package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.DesignationGroup;

import java.util.List;

public interface DesignationGroupService {

    List<DesignationGroup> getAllDesignationGroups(Long desigId);

    DesignationGroup getDesignationGroupById(Long id);

    DesignationGroup createDesignationGroup(
            DesignationGroup designationGroup);

    DesignationGroup updateDesignationGroup(
            Long id, DesignationGroup designationGroup);

    void deleteDesignationGroup(Long id);
}
