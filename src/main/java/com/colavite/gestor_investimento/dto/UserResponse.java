package com.colavite.gestor_investimento.dto;

import com.colavite.gestor_investimento.entity.Role;

import java.time.Instant;

public record UserResponse(Long id, String nome, String username, String email, Role role, Instant createdAt) {
}
