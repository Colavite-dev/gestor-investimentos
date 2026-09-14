package com.colavite.gestor_investimento.security;

import com.colavite.gestor_investimento.exception.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedUserIdTest {
    @Test
    void resolvesStableNumericSubject() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated("42", null, java.util.List.of());
        assertThat(AuthenticatedUserId.from(authentication)).isEqualTo(42L);
    }

    @Test
    void rejectsMissingUnauthenticatedAndNonNumericPrincipals() {
        assertThatThrownBy(() -> AuthenticatedUserId.from(null)).isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> AuthenticatedUserId.from(UsernamePasswordAuthenticationToken.unauthenticated("42", null)))
                .isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> AuthenticatedUserId.from(UsernamePasswordAuthenticationToken.authenticated("alice", null, java.util.List.of())))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}
