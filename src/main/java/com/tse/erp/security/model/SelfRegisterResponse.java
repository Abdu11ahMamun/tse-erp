package com.tse.erp.security.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SelfRegisterResponse {
    private String userName;
    private boolean active;
    private String message;
}