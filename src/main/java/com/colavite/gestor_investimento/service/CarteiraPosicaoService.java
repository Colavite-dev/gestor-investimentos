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
    public List<PosicaoResponse> listarPosicoes(Long carteiraId, Long usuarioId) {
        garantirCarteira(carteiraId, usuarioId);
        Map<Long, Acumulado> acumulados = new LinkedHashMap<>();
        for (Operacao op : operacoes.findByCarteiraIdOrderByDataOperacaoAscIdAsc(carteiraId)) {
            Acumulado a = acumulados.computeIfAbsent(op.getAcao().getId(), id -> new Acumulado(op.getAcao()));
            if (op.getTipo() == TipoOperacao.COMPRA) { a.quantidade = a.quantidade.add(op.getQuantidade()); a.custo = a.custo.add(op.getQuantidade().multiply(op.getPrecoUnitario())); }
            else { a.registrarVenda(op.getQuantidade()); }
        }
        return acumulados.values().stream().filter(a -> a.quantidade.signum() > 0).sorted(Comparator.comparing(a -> a.acao.getTicker())).map(Acumulado::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CarteiraResumoResponse resumo(Long carteiraId, Long usuarioId) {
        List<PosicaoResponse> posicoes = listarPosicoes(carteiraId, usuarioId);
        Map<Moeda, ResumoMoedaResponse> resumo = new EnumMap<>(Moeda.class);
        for (Moeda moeda : Moeda.values()) {
            List<PosicaoResponse> ps = posicoes.stream().filter(p -> p.moeda() == moeda).toList();
            if (ps.isEmpty()) continue;
            BigDecimal investido = ps.stream().map(PosicaoResponse::valorInvestido).reduce(BigDecimal.ZERO, BigDecimal::add);
            boolean cotacoesCompletas = ps.stream().allMatch(p -> p.patrimonioAtual() != null);
            BigDecimal patrimonio = cotacoesCompletas ? ps.stream().map(PosicaoResponse::patrimonioAtual).reduce(BigDecimal.ZERO, BigDecimal::add) : null;
            BigDecimal resultado = patrimonio == null ? null : patrimonio.subtract(investido);
            resumo.put(moeda, new ResumoMoedaResponse(ps.size(), investido, patrimonio, resultado, rentabilidade(resultado, investido)));
        }
        return new CarteiraResumoResponse(carteiraId, resumo);
    }
    private void garantirCarteira(Long id, Long usuarioId) { carteiras.findByIdAndUsuarioId(id, usuarioId).orElseThrow(() -> CarteiraNotFoundException.porId(id)); }
    private static final class Acumulado {
        final Acao acao; BigDecimal quantidade = BigDecimal.ZERO; BigDecimal custo = BigDecimal.ZERO;
        Acumulado(Acao acao) { this.acao = acao; }
        void registrarVenda(BigDecimal quantidadeVendida) {
            if (quantidadeVendida.compareTo(quantidade) > 0) throw new IllegalStateException("Operações persistidas violam o saldo não negativo da posição");
            if (quantidadeVendida.compareTo(quantidade) == 0) { quantidade = BigDecimal.ZERO; custo = BigDecimal.ZERO; return; }
            BigDecimal precoMedio = custo.divide(quantidade, 8, java.math.RoundingMode.HALF_UP);
            quantidade = quantidade.subtract(quantidadeVendida);
            custo = custo.subtract(quantidadeVendida.multiply(precoMedio));
        }
        PosicaoResponse toResponse() {
            BigDecimal medio = custo.divide(quantidade, 8, java.math.RoundingMode.HALF_UP);
            BigDecimal cotacao = acao.getCotacaoAtual();
            BigDecimal patrimonio = cotacao == null ? null : quantidade.multiply(cotacao);
            BigDecimal resultado = patrimonio == null ? null : patrimonio.subtract(custo);
            return new PosicaoResponse(acao.getId(), acao.getTicker(), acao.getMercado(), acao.getMoeda(), quantidade,
                    medio, custo, cotacao, patrimonio, resultado, rentabilidade(resultado, custo));
        }
    }
    private static BigDecimal rentabilidade(BigDecimal resultado, BigDecimal custo) {
        if (resultado == null || custo == null || custo.signum() == 0) return null;
        return resultado.divide(custo, 10, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(8, java.math.RoundingMode.HALF_UP);
    }
}
