package com.colavite.gestor_investimento.support;

import com.colavite.gestor_investimento.entity.Role;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.repository.UsuarioRepository;

import java.util.UUID;

public final class TestUsuarios {
    private TestUsuarios() {
    }

    public static Usuario novo(String identificador) {
        String suffix = identificador.toLowerCase().replaceAll("[^a-z0-9]", "") + "-" + UUID.randomUUID();
        return new Usuario("Usuário " + identificador, "user-" + suffix, "user-" + suffix + "@example.test", "test-hash", Role.USER);
    }

    public static Usuario persistir(UsuarioRepository repository, String identificador) {
        return repository.saveAndFlush(novo(identificador));
    }
}
