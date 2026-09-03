package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.*;
import com.colavite.gestor_investimento.entity.*;
import com.colavite.gestor_investimento.exception.CarteiraNotFoundException;
import com.colavite.gestor_investimento.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class CarteiraPosicaoService {
    private final CarteiraRepository carteiras; private final OperacaoRepository operacoes;
    public CarteiraPosicaoService(CarteiraRepository carteiras, OperacaoRepository operacoes) { this.carteiras = carteiras; this.operacoes = operacoes; }

    @Transactional(readOnly = true)
    public List<PosicaoResponse> listarPosicoes(Long carteiraId) {
        garantirCarteira(carteiraId);
        Map<Long, Acumulado> acumulados = new LinkedHashMap<>();
        for (Operacao op : operacoes.findByCarteiraIdOrderByDataOperacaoAscIdAsc(carteiraId)) {
            Acumulado a = acumulados.computeIfAbsent(op.getAcao().getId(), id -> new Acumulado(op.getAcao()));
            if (op.getTipo() == TipoOperacao.COMPRA) { a.quantidade = a.quantidade.add(op.getQuantidade()); a.custo = a.custo.add(op.getQuantidade().multiply(op.getPrecoUnitario())); }
            else { BigDecimal reduzir = a.quantidade.signum() > 0 ? op.getQuantidade().min(a.quantidade) : BigDecimal.ZERO; BigDecimal medio = a.quantidade.signum() > 0 ? a.custo.divide(a.quantidade, 8, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO; a.quantidade = a.quantidade.subtract(reduzir); a.custo = a.custo.subtract(reduzir.multiply(medio)).max(BigDecimal.ZERO); }
        }
        return acumulados.values().stream().filter(a -> a.quantidade.signum() > 0).sorted(Comparator.comparing(a -> a.acao.getTicker())).map(Acumulado::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CarteiraResumoResponse resumo(Long carteiraId) {
        List<PosicaoResponse> posicoes = listarPosicoes(carteiraId);
        Map<Moeda, ResumoMoedaResponse> resumo = new EnumMap<>(Moeda.class);
        for (Moeda moeda : Moeda.values()) {
            List<PosicaoResponse> ps = posicoes.stream().filter(p -> p.moeda() == moeda).toList();
            if (ps.isEmpty()) continue;
            BigDecimal investido = ps.stream().map(PosicaoResponse::valorInvestido).reduce(BigDecimal.ZERO, BigDecimal::add);
            boolean cotacoesCompletas = ps.stream().allMatch(p -> p.patrimonioAtual() != null);
            BigDecimal patrimonio = cotacoesCompletas ? ps.stream().map(PosicaoResponse::patrimonioAtual).reduce(BigDecimal.ZERO, BigDecimal::add) : null;
            resumo.put(moeda, new ResumoMoedaResponse(ps.size(), investido, patrimonio, patrimonio == null ? null : patrimonio.subtract(investido)));
        }
        return new CarteiraResumoResponse(carteiraId, resumo);
    }
    private void garantirCarteira(Long id) { if (!carteiras.existsById(id)) throw CarteiraNotFoundException.porId(id); }
    private static final class Acumulado {
        final Acao acao; BigDecimal quantidade = BigDecimal.ZERO; BigDecimal custo = BigDecimal.ZERO;
        Acumulado(Acao acao) { this.acao = acao; }
        PosicaoResponse toResponse() { BigDecimal medio = custo.divide(quantidade, 8, java.math.RoundingMode.HALF_UP); BigDecimal patrimonio = acao.getCotacaoAtual() == null ? null : quantidade.multiply(acao.getCotacaoAtual()); return new PosicaoResponse(acao.getId(), acao.getTicker(), acao.getMercado(), acao.getMoeda(), quantidade, medio, custo, patrimonio, patrimonio == null ? null : patrimonio.subtract(custo)); }
    }
}
