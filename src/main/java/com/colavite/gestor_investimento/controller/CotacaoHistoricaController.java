package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.dto.CotacaoHistoricaResponse;
import com.colavite.gestor_investimento.service.CotacaoHistoricaService;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@Validated
@RestController
@RequestMapping("/acoes/{acaoId}/historico-cotacoes")
public class CotacaoHistoricaController {
    private final CotacaoHistoricaService service;
    public CotacaoHistoricaController(CotacaoHistoricaService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<CotacaoHistoricaResponse>> listar(
            @PathVariable @Positive(message = "ID deve ser maior que zero") Long acaoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant ate) {
        return ResponseEntity.ok(service.listar(acaoId, de, ate));
    }
}
