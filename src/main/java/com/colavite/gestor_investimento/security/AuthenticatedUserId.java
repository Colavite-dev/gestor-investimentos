package com.colavite.gestor_investimento.security;

import com.colavite.gestor_investimento.exception.CredenciaisInvalidasException;
import org.springframework.security.core.Authentication;

public final class AuthenticatedUserId {
    private AuthenticatedUserId() {
    }

    public static Long from(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CredenciaisInvalidasException();
        }
        try {
            return Long.valueOf(authentication.getName());
        }
        catch (NumberFormatException | NullPointerException exception) {
            throw new CredenciaisInvalidasException();
        }
    }
}
