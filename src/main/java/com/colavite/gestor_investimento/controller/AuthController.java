package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.dto.LoginRequest;
import com.colavite.gestor_investimento.dto.LoginResponse;
import com.colavite.gestor_investimento.dto.RegisterRequest;
import com.colavite.gestor_investimento.dto.UserResponse;
import com.colavite.gestor_investimento.dto.AuthenticatedUserResponse;
import com.colavite.gestor_investimento.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.currentUser(Long.valueOf(authentication.getName())));
    }
}
