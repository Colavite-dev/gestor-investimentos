package com.colavite.gestor_investimento.repository;
import com.colavite.gestor_investimento.entity.Operacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface OperacaoRepository extends JpaRepository<Operacao, Long> {
    Optional<Operacao> findByIdAndCarteiraUsuarioId(Long id, Long usuarioId);
    List<Operacao> findByCarteiraIdOrderByDataOperacaoAscIdAsc(Long carteiraId);
    List<Operacao> findByCarteiraIdAndAcaoIdOrderByDataOperacaoAscIdAsc(Long carteiraId, Long acaoId);
}
