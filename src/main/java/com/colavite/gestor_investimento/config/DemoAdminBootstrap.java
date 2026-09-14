package com.colavite.gestor_investimento.config;

import com.colavite.gestor_investimento.entity.Role;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoAdminBootstrap {
    @Bean
    @ConditionalOnProperty(name = "app.demo-admin.enabled", havingValue = "true")
    public ApplicationRunner demoAdminRunner(UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
                                              @Value("${app.demo-admin.username:}") String username,
                                              @Value("${app.demo-admin.password:}") String password) {
        return ignored -> {
            String configuredUsername = username == null ? "" : username.trim();
            if (configuredUsername.isBlank() || configuredUsername.length() > 60 || password == null || password.isBlank() || password.length() > 200) return;
            usuarios.findByUsername(configuredUsername).ifPresentOrElse(existing -> {
            if (existing.getRole() != Role.ADMIN) {
                throw new IllegalStateException("O usuário de demonstração configurado já existe sem papel ADMIN");
            }
            }, () -> usuarios.save(new Usuario("Administrador de demonstração", configuredUsername,
                    configuredUsername + "@demo.local.invalid", passwordEncoder.encode(password), Role.ADMIN)));
        };
    }
}
