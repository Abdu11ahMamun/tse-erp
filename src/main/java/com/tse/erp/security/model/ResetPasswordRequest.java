package com.tse.erp.security.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class  ResetPasswordRequest{
private String resetToken;
private String newPassword;
private String confirmPassword;
    }