package com.colavite.gestor_investimento.config;

import com.colavite.gestor_investimento.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "app.demo-admin.enabled=false")
@ActiveProfiles("test")
class DemoAdminBootstrapDisabledTest {
    @Autowired ApplicationContext context;
    @Autowired UsuarioRepository usuarios;

    @BeforeEach
    void clearUsers() {
        usuarios.deleteAll();
    }

    @Test
    void doesNotCreateAdminOrExposeBootstrapWhenDisabled() {
        assertThat(context.containsBean("demoAdminRunner")).isFalse();
        assertThat(usuarios.findByUsername("adm")).isEmpty();
    }
}
