package com.colavite.gestor_investimento.config;

import com.colavite.gestor_investimento.entity.Role;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "app.demo-admin.enabled=false")
@ActiveProfiles("test")
class DemoAdminBootstrapEnabledTest {
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwordEncoder;
    private final DemoAdminBootstrap bootstrap = new DemoAdminBootstrap();

    @BeforeEach
    void recreateAdmin() throws Exception {
        usuarios.deleteAll();
        usuarios.flush();
        bootstrap.demoAdminRunner(usuarios, passwordEncoder, "bootstrap-test-admin", "bootstrap-test-password").run(null);
    }

    @Test
    void createsHashedAdminAndIsIdempotent() throws Exception {
        Usuario admin = usuarios.findByUsername("bootstrap-test-admin").orElseThrow();
        String hash = admin.getPasswordHash();
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(hash).isNotEqualTo("bootstrap-test-password");
        assertThat(passwordEncoder.matches("bootstrap-test-password", hash)).isTrue();

        bootstrap.demoAdminRunner(usuarios, passwordEncoder, "bootstrap-test-admin", "bootstrap-test-password").run(null);

        assertThat(usuarios.count()).isEqualTo(1);
        assertThat(usuarios.findByUsername("bootstrap-test-admin").orElseThrow().getPasswordHash()).isEqualTo(hash);
    }

    @Test
    void failsSafelyWhenAdmAlreadyExistsAsUser() {
        usuarios.deleteAll();
        usuarios.saveAndFlush(new Usuario("Usuário", "bootstrap-test-admin", "user@example.com", passwordEncoder.encode("outra-senha"), Role.USER));

        assertThatThrownBy(() -> bootstrap.demoAdminRunner(usuarios, passwordEncoder, "bootstrap-test-admin", "bootstrap-test-password").run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem papel ADMIN");
        assertThat(usuarios.findByUsername("bootstrap-test-admin").orElseThrow().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void skipsBootstrapWhenExternalUsernameOrPasswordIsMissingOrInvalid() throws Exception {
        usuarios.deleteAll();
        usuarios.flush();
        bootstrap.demoAdminRunner(usuarios, passwordEncoder, "", "bootstrap-test-password").run(null);
        bootstrap.demoAdminRunner(usuarios, passwordEncoder, "bootstrap-test-admin", "").run(null);
        bootstrap.demoAdminRunner(usuarios, passwordEncoder, "x".repeat(61), "bootstrap-test-password").run(null);

        assertThat(usuarios.count()).isZero();
    }
}
