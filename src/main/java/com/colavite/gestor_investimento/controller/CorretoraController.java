package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.dto.CorretoraResponse;
import com.colavite.gestor_investimento.service.CorretoraService;
import com.colavite.gestor_investimento.validation.ValidCnpj;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/corretoras")
public class CorretoraController {

    private final CorretoraService service;

    public CorretoraController(CorretoraService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CorretoraResponse> cadastrar(@Valid @RequestBody CorretoraRequest request) {
        CorretoraResponse response = service.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CorretoraResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorretoraResponse> buscarPorId(
            @PathVariable @Positive(message = "ID deve ser maior que zero") Long id
    ) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<CorretoraResponse> buscarPorCnpj(
            @PathVariable
            @Pattern(regexp = "\\d{14}", message = "CNPJ no path deve conter 14 dígitos")
            @ValidCnpj
            String cnpj
    ) {
        return ResponseEntity.ok(service.buscarPorCnpj(cnpj));
    }
}
