package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.Corretora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CorretoraRepository extends JpaRepository<Corretora, Long> {

    boolean existsByCnpj(String cnpj);

    Optional<Corretora> findByCnpj(String cnpj);

    List<Corretora> findAllByOrderByIdAsc();
}
