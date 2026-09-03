package com.colavite.gestor_investimento.repository;
import com.colavite.gestor_investimento.entity.Carteira;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CarteiraRepository extends JpaRepository<Carteira, Long> {
    boolean existsByNomeNormalizado(String nomeNormalizado);
    List<Carteira> findAllByOrderByIdAsc();
}
