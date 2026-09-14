package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CarteiraRequest;
import com.colavite.gestor_investimento.exception.CarteiraDuplicadaException;
import com.colavite.gestor_investimento.repository.CarteiraRepository;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import com.colavite.gestor_investimento.support.TestUsuarios;
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
    @Mock UsuarioRepository usuarios;
    @Test void normalizaNomeEConservaDescricao() {
        var usuario = TestUsuarios.novo("owner");
        CarteiraService service = new CarteiraService(repository, usuarios);
        when(repository.existsByUsuarioIdAndNomeNormalizado(1L, "MINHA CARTEIRA")).thenReturn(false);
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var response = service.cadastrar(new CarteiraRequest("  Minha Carteira  ", "  longo prazo  "), 1L);
        assertThat(response.nome()).isEqualTo("Minha Carteira");
        assertThat(response.descricao()).isEqualTo("longo prazo");
    }
    @Test void rejeitaNomeDuplicadoSemPersistir() {
        CarteiraService service = new CarteiraService(repository, usuarios);
        when(repository.existsByUsuarioIdAndNomeNormalizado(1L, "MINHA CARTEIRA")).thenReturn(true);
        assertThatThrownBy(() -> service.cadastrar(new CarteiraRequest("minha carteira", null), 1L)).isInstanceOf(CarteiraDuplicadaException.class);
        verify(repository, never()).saveAndFlush(any());
    }
}
