package com.tse.erp.security.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SelfRegisterRequest {
    private String userName;
    private String fullName;
    private String email;
    private String mobileNo;
    private String employeeCode;
    private String password;
    private String confirmPassword;
}