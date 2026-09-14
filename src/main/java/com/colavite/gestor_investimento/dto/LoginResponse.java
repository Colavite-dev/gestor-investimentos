package com.colavite.gestor_investimento.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, AuthenticatedUserResponse user) {
}
