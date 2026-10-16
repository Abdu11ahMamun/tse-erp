package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.entity.UserTypeGroup;

import java.util.List;

public interface UserTypeGroupMapService {

    List<UserTypeGroup> getAll(Long userTypeId, Long buId);

    UserTypeGroup getById(Long id);

    UserTypeGroup create(UserTypeGroup row);

    UserTypeGroup update(Long id, UserTypeGroup row);

    void delete(Long id);
}
