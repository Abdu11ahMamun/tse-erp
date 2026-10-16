package com.tse.erp.security;

import java.security.Principal;

public record AuthUser(Long userId, String username, Long empId,
                       Long bgId, Long buId) implements Principal {
    @Override
    public String getName() { return username; }

    @Override
    public String toString() { return username; }
}