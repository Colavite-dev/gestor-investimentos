package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.CotacaoHistorica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface CotacaoHistoricaRepository extends JpaRepository<CotacaoHistorica, Long> {
    List<CotacaoHistorica> findByAcaoIdOrderByDataHoraCotacaoAscIdAsc(Long acaoId);
    List<CotacaoHistorica> findByAcaoIdAndDataHoraCotacaoGreaterThanEqualAndDataHoraCotacaoLessThanEqualOrderByDataHoraCotacaoAscIdAsc(
            Long acaoId, Instant de, Instant ate);
    List<CotacaoHistorica> findByAcaoIdAndDataHoraCotacaoGreaterThanEqualOrderByDataHoraCotacaoAscIdAsc(Long acaoId, Instant de);
    List<CotacaoHistorica> findByAcaoIdAndDataHoraCotacaoLessThanEqualOrderByDataHoraCotacaoAscIdAsc(Long acaoId, Instant ate);
}
