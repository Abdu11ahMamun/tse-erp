package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.dto.UserTypeGroupDto;

public interface UserTypeGroupService {

    UserTypeGroupDto getByUserType(Long userTypeId);

    UserTypeGroupDto save(Long userTypeId, UserTypeGroupDto dto);
}
