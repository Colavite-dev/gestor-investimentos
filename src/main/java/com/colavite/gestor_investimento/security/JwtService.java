package com.colavite.gestor_investimento.security;

import com.colavite.gestor_investimento.dto.LoginResponse;
import com.colavite.gestor_investimento.dto.AuthenticatedUserResponse;
import com.colavite.gestor_investimento.entity.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {
    public static final Duration TOKEN_TTL = Duration.ofMinutes(60);
    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponse issue(Usuario usuario, AuthenticatedUserResponse user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(TOKEN_TTL);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(usuario.getId().toString())
                .claim("username", usuario.getUsername())
                .claim("role", usuario.getRole().name())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", TOKEN_TTL.toSeconds(), user);
    }
}
