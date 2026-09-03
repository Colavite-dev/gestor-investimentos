package com.colavite.gestor_investimento.dto;
import java.time.Instant;
public record CarteiraResponse(Long id, String nome, String descricao, Instant dataCadastro) {}
