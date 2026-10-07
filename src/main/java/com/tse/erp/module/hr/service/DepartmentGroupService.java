package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.DepartmentGroup;

import java.util.List;

public interface DepartmentGroupService {

    List<DepartmentGroup> getAllDepartmentGroups(Long deptId);

    DepartmentGroup getDepartmentGroupById(Long id);

    DepartmentGroup createDepartmentGroup(DepartmentGroup departmentGroup);

    DepartmentGroup updateDepartmentGroup(
            Long id, DepartmentGroup departmentGroup);

    void deleteDepartmentGroup(Long id);
}
