package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.Corretora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CorretoraRepository extends JpaRepository<Corretora, Long> {

    boolean existsByUsuarioIdAndCnpj(Long usuarioId, String cnpj);

    Optional<Corretora> findByIdAndUsuarioId(Long id, Long usuarioId);

    Optional<Corretora> findByUsuarioIdAndCnpj(Long usuarioId, String cnpj);

    List<Corretora> findAllByUsuarioIdOrderByIdAsc(Long usuarioId);
}
