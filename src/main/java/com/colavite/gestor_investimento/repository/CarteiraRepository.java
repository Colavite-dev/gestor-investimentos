package com.colavite.gestor_investimento.repository;
import com.colavite.gestor_investimento.entity.Carteira;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface CarteiraRepository extends JpaRepository<Carteira, Long> {
    boolean existsByUsuarioIdAndNomeNormalizado(Long usuarioId, String nomeNormalizado);
    List<Carteira> findAllByUsuarioIdOrderByIdAsc(Long usuarioId);
    Optional<Carteira> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select carteira from Carteira carteira where carteira.id = :id and carteira.usuario.id = :usuarioId")
    Optional<Carteira> findByIdAndUsuarioIdForUpdate(@Param("id") Long id, @Param("usuarioId") Long usuarioId);
}
