package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcaoRepository extends JpaRepository<Acao, Long> {
    boolean existsByTickerAndMercado(String ticker, Mercado mercado);
    Optional<Acao> findByTickerAndMercado(String ticker, Mercado mercado);
    List<Acao> findByTicker(String ticker);
    List<Acao> findAllByOrderByIdAsc();
}
