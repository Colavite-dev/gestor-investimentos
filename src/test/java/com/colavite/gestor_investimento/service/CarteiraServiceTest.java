package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CarteiraRequest;
import com.colavite.gestor_investimento.exception.CarteiraDuplicadaException;
import com.colavite.gestor_investimento.repository.CarteiraRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarteiraServiceTest {
    @Mock CarteiraRepository repository;
    @Test void normalizaNomeEConservaDescricao() {
        CarteiraService service = new CarteiraService(repository);
        when(repository.existsByNomeNormalizado("MINHA CARTEIRA")).thenReturn(false);
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var response = service.cadastrar(new CarteiraRequest("  Minha Carteira  ", "  longo prazo  "));
        assertThat(response.nome()).isEqualTo("Minha Carteira");
        assertThat(response.descricao()).isEqualTo("longo prazo");
    }
    @Test void rejeitaNomeDuplicadoSemPersistir() {
        CarteiraService service = new CarteiraService(repository);
        when(repository.existsByNomeNormalizado("MINHA CARTEIRA")).thenReturn(true);
        assertThatThrownBy(() -> service.cadastrar(new CarteiraRequest("minha carteira", null))).isInstanceOf(CarteiraDuplicadaException.class);
        verify(repository, never()).saveAndFlush(any());
    }
}
