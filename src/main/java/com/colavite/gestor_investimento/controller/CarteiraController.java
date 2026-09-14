package com.colavite.gestor_investimento.controller;
import com.colavite.gestor_investimento.dto.*;
import com.colavite.gestor_investimento.service.CarteiraService;
import com.colavite.gestor_investimento.service.OperacaoService;
import com.colavite.gestor_investimento.service.CarteiraPosicaoService;
import com.colavite.gestor_investimento.service.CarteiraQuoteRefreshService;
import com.colavite.gestor_investimento.dto.PosicaoResponse;
import com.colavite.gestor_investimento.dto.CarteiraResumoResponse;
import com.colavite.gestor_investimento.dto.OperacaoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.colavite.gestor_investimento.security.AuthenticatedUserId;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@RestController @RequestMapping("/carteiras")
public class CarteiraController {
    private final CarteiraService service;
    private final OperacaoService operacaoService;
    private final CarteiraPosicaoService posicaoService;
    private final CarteiraQuoteRefreshService quoteRefreshService;
    public CarteiraController(CarteiraService service, OperacaoService operacaoService, CarteiraPosicaoService posicaoService, CarteiraQuoteRefreshService quoteRefreshService) { this.service = service; this.operacaoService = operacaoService; this.posicaoService = posicaoService; this.quoteRefreshService = quoteRefreshService; }
    @PostMapping public ResponseEntity<CarteiraResponse> cadastrar(@Valid @RequestBody CarteiraRequest request, Authentication authentication) {
        CarteiraResponse response = service.cadastrar(request, AuthenticatedUserId.from(authentication));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping public ResponseEntity<List<CarteiraResponse>> listar(Authentication authentication) { return ResponseEntity.ok(service.listar(AuthenticatedUserId.from(authentication))); }
    @GetMapping("/{id}") public ResponseEntity<CarteiraResponse> buscarPorId(@PathVariable @Positive(message = "ID deve ser maior que zero") Long id, Authentication authentication) { return ResponseEntity.ok(service.buscarPorId(id, AuthenticatedUserId.from(authentication))); }
    @GetMapping("/{id}/operacoes") public ResponseEntity<List<OperacaoResponse>> listarOperacoes(@PathVariable @Positive Long id, Authentication authentication) { return ResponseEntity.ok(operacaoService.listarPorCarteira(id, AuthenticatedUserId.from(authentication))); }
    @GetMapping("/{id}/posicoes") public ResponseEntity<List<PosicaoResponse>> listarPosicoes(@PathVariable @Positive Long id, Authentication authentication) { return ResponseEntity.ok(posicaoService.listarPosicoes(id, AuthenticatedUserId.from(authentication))); }
    @GetMapping("/{id}/resumo") public ResponseEntity<CarteiraResumoResponse> resumo(@PathVariable @Positive Long id, Authentication authentication) { return ResponseEntity.ok(posicaoService.resumo(id, AuthenticatedUserId.from(authentication))); }
    @PutMapping("/{id}/atualizar-cotacoes") public ResponseEntity<CarteiraQuoteRefreshResponse> atualizarCotacoes(@PathVariable @Positive Long id, Authentication authentication) { return ResponseEntity.ok(quoteRefreshService.atualizarCotacoes(id, AuthenticatedUserId.from(authentication))); }
}
