package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.LoginRequest;
import com.colavite.gestor_investimento.dto.LoginResponse;
import com.colavite.gestor_investimento.dto.RegisterRequest;
import com.colavite.gestor_investimento.dto.UserResponse;
import com.colavite.gestor_investimento.dto.AuthenticatedUserResponse;
import com.colavite.gestor_investimento.entity.Role;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.exception.CredenciaisInvalidasException;
import com.colavite.gestor_investimento.exception.UsuarioDuplicadoException;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import com.colavite.gestor_investimento.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = normalize(request.username());
        String email = normalize(request.email());
        String nome = request.nome().trim();
        if (nome.isEmpty() || username.isEmpty() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Dados de usuário inválidos");
        }
        if (usuarios.existsByUsername(username) || usuarios.existsByEmail(email)) {
            throw new UsuarioDuplicadoException();
        }
        try {
            Usuario usuario = usuarios.saveAndFlush(new Usuario(nome, username, email, passwordEncoder.encode(request.password()), Role.USER));
            return toResponse(usuario);
        }
        catch (DataIntegrityViolationException exception) {
            throw new UsuarioDuplicadoException();
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByUsername(normalize(request.username()))
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(CredenciaisInvalidasException::new);
        return jwtService.issue(usuario, toAuthenticatedResponse(usuario));
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse currentUser(Long id) {
        Usuario usuario = usuarios.findById(id).orElseThrow(CredenciaisInvalidasException::new);
        return toAuthenticatedResponse(usuario);
    }

    public static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static UserResponse toResponse(Usuario usuario) {
        return new UserResponse(usuario.getId(), usuario.getNome(), usuario.getUsername(), usuario.getEmail(), usuario.getRole(), usuario.getCreatedAt());
    }

    private static AuthenticatedUserResponse toAuthenticatedResponse(Usuario usuario) {
        return new AuthenticatedUserResponse(usuario.getId(), usuario.getNome(), usuario.getUsername(), usuario.getRole());
    }
}
