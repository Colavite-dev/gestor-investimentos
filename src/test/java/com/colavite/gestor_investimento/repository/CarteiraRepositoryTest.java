package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.Carteira;
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
    @Test void persisteCarteiraEV3() {
        Carteira carteira = repository.saveAndFlush(new Carteira("Longo prazo", "LONGO PRAZO", "Ações"));
        assertThat(repository.findById(carteira.getId())).isPresent();
    }
    @Test void constraintProtegeNomeNormalizado() {
        repository.saveAndFlush(new Carteira("A", "A", null));
        assertThatThrownBy(() -> repository.saveAndFlush(new Carteira("Outro", "A", null))).isInstanceOf(DataIntegrityViolationException.class);
    }
}
