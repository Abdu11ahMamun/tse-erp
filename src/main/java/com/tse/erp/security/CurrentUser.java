package com.tse.erp.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class CurrentUser {

    private CurrentUser() {}

    public static Optional<AuthUser> get() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof AuthUser u) {
            return Optional.of(u);
        }
        return Optional.empty();
    }
}