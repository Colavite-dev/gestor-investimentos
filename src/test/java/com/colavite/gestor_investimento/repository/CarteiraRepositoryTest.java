package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.Carteira;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test") @Transactional
class CarteiraRepositoryTest {
    @Autowired CarteiraRepository repository;
    @Autowired UsuarioRepository usuarios;
    @Test void persisteCarteiraEV3() {
        var usuario = TestUsuarios.persistir(usuarios, "repo-persist");
        Carteira carteira = repository.saveAndFlush(new Carteira("Longo prazo", "LONGO PRAZO", "Ações", usuario));
        assertThat(repository.findById(carteira.getId())).isPresent();
    }
    @Test void constraintProtegeNomeNormalizado() {
        var usuario = TestUsuarios.persistir(usuarios, "repo-unique");
        repository.saveAndFlush(new Carteira("A", "A", null, usuario));
        assertThatThrownBy(() -> repository.saveAndFlush(new Carteira("Outro", "A", null, usuario))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void permiteMesmoNomeParaOwnersDiferentesEScopaConsultas() {
        var ownerA = TestUsuarios.persistir(usuarios, "repo-owner-a");
        var ownerB = TestUsuarios.persistir(usuarios, "repo-owner-b");
        var carteiraA = repository.saveAndFlush(new Carteira("Reserva", "RESERVA", null, ownerA));
        var carteiraB = repository.saveAndFlush(new Carteira("Reserva", "RESERVA", null, ownerB));

        assertThat(repository.findAllByUsuarioIdOrderByIdAsc(ownerA.getId())).containsExactly(carteiraA);
        assertThat(repository.findByIdAndUsuarioId(carteiraA.getId(), ownerB.getId())).isEmpty();
        assertThat(repository.findByIdAndUsuarioIdForUpdate(carteiraB.getId(), ownerB.getId())).contains(carteiraB);
    }
}
