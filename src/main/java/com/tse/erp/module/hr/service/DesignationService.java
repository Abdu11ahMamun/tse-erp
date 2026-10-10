package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.Designation;

import java.util.List;

public interface DesignationService {

    List<Designation> getAllDesignations(Long bgId);

    Designation getDesignationById(Long id);

    Designation createDesignation(Designation designation);

    Designation updateDesignation(Long id, Designation designation);

    void deleteDesignation(Long id);
}
