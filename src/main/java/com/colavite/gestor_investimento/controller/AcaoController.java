package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.dto.AcaoRequest;
import com.colavite.gestor_investimento.dto.AcaoResponse;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.service.AcaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/acoes")
public class AcaoController {

    private final AcaoService service;

    public AcaoController(AcaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AcaoResponse> cadastrar(@Valid @RequestBody AcaoRequest request) {
        AcaoResponse response = service.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AcaoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/ticker/{ticker}")
    public ResponseEntity<AcaoResponse> buscarPorTicker(
            @PathVariable @NotBlank @Size(max = 20) @Pattern(regexp = "[A-Za-z0-9.-]+", message = "Ticker contém caracteres inválidos") String ticker,
            @RequestParam(required = false) Mercado mercado
    ) {
        return ResponseEntity.ok(service.buscarPorTicker(ticker, mercado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcaoResponse> buscarPorId(@PathVariable @Positive(message = "ID deve ser maior que zero") Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}/atualizar-cotacao")
    public ResponseEntity<AcaoResponse> atualizarCotacao(@PathVariable @Positive(message = "ID deve ser maior que zero") Long id) {
        return ResponseEntity.ok(service.atualizarCotacao(id));
    }
}
