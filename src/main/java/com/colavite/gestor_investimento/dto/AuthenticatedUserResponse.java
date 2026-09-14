package com.colavite.gestor_investimento.dto;

import com.colavite.gestor_investimento.entity.Role;

public record AuthenticatedUserResponse(Long id, String nome, String username, Role role) {
}
