package com.colavite.gestor_investimento.service;
import com.colavite.gestor_investimento.dto.*;
import com.colavite.gestor_investimento.entity.Operacao;
import com.colavite.gestor_investimento.exception.*;
import com.colavite.gestor_investimento.mapper.OperacaoMapper;
import com.colavite.gestor_investimento.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service public class OperacaoService {
    private final OperacaoRepository operacoes; private final CarteiraRepository carteiras; private final AcaoRepository acoes;
    public OperacaoService(OperacaoRepository operacoes, CarteiraRepository carteiras, AcaoRepository acoes) { this.operacoes=operacoes; this.carteiras=carteiras; this.acoes=acoes; }
    @Transactional public OperacaoResponse cadastrar(OperacaoRequest r) {
        var carteira = carteiras.findById(r.carteiraId()).orElseThrow(() -> com.colavite.gestor_investimento.exception.CarteiraNotFoundException.porId(r.carteiraId()));
        var acao = acoes.findById(r.acaoId()).orElseThrow(() -> AcaoNotFoundException.porId(r.acaoId()));
        return OperacaoMapper.toResponse(operacoes.saveAndFlush(new Operacao(carteira, acao, r.tipo(), r.quantidade(), r.precoUnitario(), r.dataOperacao())));
    }
    @Transactional(readOnly=true) public OperacaoResponse buscarPorId(Long id) { return operacoes.findById(id).map(OperacaoMapper::toResponse).orElseThrow(() -> OperacaoNotFoundException.porId(id)); }
    @Transactional(readOnly=true) public List<OperacaoResponse> listarPorCarteira(Long id) { if (!carteiras.existsById(id)) throw CarteiraNotFoundException.porId(id); return operacoes.findByCarteiraIdOrderByDataOperacaoAscIdAsc(id).stream().map(OperacaoMapper::toResponse).toList(); }
}
