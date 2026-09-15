package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CorretoraRepositoryTest {
    @Autowired private CorretoraRepository repository;
    @Autowired private UsuarioRepository usuarios;

    @Test
    void ownerScopedQueriesNaoRetornamCorretorasDeOutroUsuario() {
        Usuario ownerA = TestUsuarios.persistir(usuarios, "broker-repo-a");
        Usuario ownerB = TestUsuarios.persistir(usuarios, "broker-repo-b");
        Corretora corretoraA = repository.saveAndFlush(entity("11222333000181", ownerA));
        Corretora corretoraB = repository.saveAndFlush(entity("45723174000110", ownerB));

        assertThat(repository.existsByUsuarioIdAndCnpj(ownerA.getId(), corretoraA.getCnpj())).isTrue();
        assertThat(repository.existsByUsuarioIdAndCnpj(ownerB.getId(), corretoraA.getCnpj())).isFalse();
        assertThat(repository.findAllByUsuarioIdOrderByIdAsc(ownerA.getId())).containsExactly(corretoraA);
        assertThat(repository.findByIdAndUsuarioId(corretoraA.getId(), ownerB.getId())).isEmpty();
        assertThat(repository.findByUsuarioIdAndCnpj(ownerB.getId(), corretoraA.getCnpj())).isEmpty();
        assertThat(repository.findByIdAndUsuarioId(corretoraB.getId(), ownerB.getId())).contains(corretoraB);
    }

    @Test
    void permiteMesmoCnpjParaOwnersDiferentesEMantemUnicidadePorOwner() {
        Usuario ownerA = TestUsuarios.persistir(usuarios, "broker-unique-a");
        Usuario ownerB = TestUsuarios.persistir(usuarios, "broker-unique-b");
        repository.saveAndFlush(entity("11222333000181", ownerA));
        repository.saveAndFlush(entity("11222333000181", ownerB));

        assertThatThrownBy(() -> repository.saveAndFlush(entity("11222333000181", ownerA)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Corretora entity(String cnpj, Usuario usuario) {
        return new Corretora(cnpj, "Corretora Exemplo S.A.", null, null, null,
                "01001000", "Praca da Se", "100", null, "Se", "Sao Paulo", "SP", "ATIVA", usuario);
    }
}
