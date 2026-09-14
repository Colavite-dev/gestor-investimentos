package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.AdminMetricsResponse;
import com.colavite.gestor_investimento.dto.UserResponse;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import com.colavite.gestor_investimento.repository.CarteiraRepository;
import com.colavite.gestor_investimento.repository.OperacaoRepository;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final UsuarioRepository usuarios;
    private final CarteiraRepository carteiras;
    private final AcaoRepository acoes;
    private final OperacaoRepository operacoes;

    public AdminService(UsuarioRepository usuarios, CarteiraRepository carteiras, AcaoRepository acoes, OperacaoRepository operacoes) {
        this.usuarios = usuarios;
        this.carteiras = carteiras;
        this.acoes = acoes;
        this.operacoes = operacoes;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return usuarios.findAllByOrderByIdAsc().stream().map(AuthService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminMetricsResponse metrics() {
        return new AdminMetricsResponse(usuarios.count(), carteiras.count(), acoes.count(), operacoes.count());
    }
}
