package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.entity.UserType;
import java.util.List;

public interface UserTypeService {
    List<UserType> getAllUserTypes();
    UserType getUserTypeById(Long id);
    UserType createUserType(UserType userType);
    UserType updateUserType(Long id, UserType userType);
    void deleteUserType(Long id);
}