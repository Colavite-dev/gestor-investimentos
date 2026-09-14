package com.colavite.gestor_investimento.service;
import com.colavite.gestor_investimento.dto.*;
import com.colavite.gestor_investimento.entity.Operacao;
import com.colavite.gestor_investimento.entity.TipoOperacao;
import com.colavite.gestor_investimento.exception.*;
import com.colavite.gestor_investimento.mapper.OperacaoMapper;
import com.colavite.gestor_investimento.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
@Service public class OperacaoService {
    private final OperacaoRepository operacoes; private final CarteiraRepository carteiras; private final AcaoRepository acoes;
    public OperacaoService(OperacaoRepository operacoes, CarteiraRepository carteiras, AcaoRepository acoes) { this.operacoes=operacoes; this.carteiras=carteiras; this.acoes=acoes; }
    @Transactional public OperacaoResponse cadastrar(OperacaoRequest r, Long usuarioId) {
        var carteira = carteiras.findByIdAndUsuarioIdForUpdate(r.carteiraId(), usuarioId).orElseThrow(() -> CarteiraNotFoundException.porId(r.carteiraId()));
        var acao = acoes.findById(r.acaoId()).orElseThrow(() -> AcaoNotFoundException.porId(r.acaoId()));
        Operacao candidata = new Operacao(carteira, acao, r.tipo(), r.quantidade(), r.precoUnitario(), r.dataOperacao());
        if (r.tipo() == TipoOperacao.VENDA) {
            validarSaldoDaSequencia(r.carteiraId(), r.acaoId(), candidata);
        }
        return OperacaoMapper.toResponse(operacoes.saveAndFlush(candidata));
    }
    @Transactional(readOnly=true) public OperacaoResponse buscarPorId(Long id, Long usuarioId) { return operacoes.findByIdAndCarteiraUsuarioId(id, usuarioId).map(OperacaoMapper::toResponse).orElseThrow(() -> OperacaoNotFoundException.porId(id)); }
    @Transactional(readOnly=true) public List<OperacaoResponse> listarPorCarteira(Long id, Long usuarioId) { carteiras.findByIdAndUsuarioId(id, usuarioId).orElseThrow(() -> CarteiraNotFoundException.porId(id)); return operacoes.findByCarteiraIdOrderByDataOperacaoAscIdAsc(id).stream().map(OperacaoMapper::toResponse).toList(); }
    private void validarSaldoDaSequencia(Long carteiraId, Long acaoId, Operacao candidata) {
        List<Operacao> sequencia = new ArrayList<>(operacoes.findByCarteiraIdAndAcaoIdOrderByDataOperacaoAscIdAsc(carteiraId, acaoId));
        sequencia.add(candidata);
        sequencia.sort(Comparator.comparing(Operacao::getDataOperacao)
                .thenComparing(Operacao::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        BigDecimal saldo = BigDecimal.ZERO;
        for (Operacao operacao : sequencia) {
            saldo = operacao.getTipo() == TipoOperacao.COMPRA ? saldo.add(operacao.getQuantidade()) : saldo.subtract(operacao.getQuantidade());
            if (saldo.signum() < 0) throw new SaldoInsuficienteParaVendaException();
        }
    }
}
