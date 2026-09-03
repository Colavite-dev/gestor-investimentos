package com.colavite.gestor_investimento.controller;
import com.colavite.gestor_investimento.dto.*;
import com.colavite.gestor_investimento.service.CarteiraService;
import com.colavite.gestor_investimento.service.OperacaoService;
import com.colavite.gestor_investimento.service.CarteiraPosicaoService;
import com.colavite.gestor_investimento.dto.PosicaoResponse;
import com.colavite.gestor_investimento.dto.CarteiraResumoResponse;
import com.colavite.gestor_investimento.dto.OperacaoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@RestController @RequestMapping("/carteiras")
public class CarteiraController {
    private final CarteiraService service;
    private final OperacaoService operacaoService;
    private final CarteiraPosicaoService posicaoService;
    public CarteiraController(CarteiraService service, OperacaoService operacaoService, CarteiraPosicaoService posicaoService) { this.service = service; this.operacaoService = operacaoService; this.posicaoService = posicaoService; }
    @PostMapping public ResponseEntity<CarteiraResponse> cadastrar(@Valid @RequestBody CarteiraRequest request) {
        CarteiraResponse response = service.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping public ResponseEntity<List<CarteiraResponse>> listar() { return ResponseEntity.ok(service.listar()); }
    @GetMapping("/{id}") public ResponseEntity<CarteiraResponse> buscarPorId(@PathVariable @Positive(message = "ID deve ser maior que zero") Long id) { return ResponseEntity.ok(service.buscarPorId(id)); }
    @GetMapping("/{id}/operacoes") public ResponseEntity<List<OperacaoResponse>> listarOperacoes(@PathVariable @Positive Long id) { return ResponseEntity.ok(operacaoService.listarPorCarteira(id)); }
    @GetMapping("/{id}/posicoes") public ResponseEntity<List<PosicaoResponse>> listarPosicoes(@PathVariable @Positive Long id) { return ResponseEntity.ok(posicaoService.listarPosicoes(id)); }
    @GetMapping("/{id}/resumo") public ResponseEntity<CarteiraResumoResponse> resumo(@PathVariable @Positive Long id) { return ResponseEntity.ok(posicaoService.resumo(id)); }
}
