package com.colavite.gestor_investimento.controller;
import com.colavite.gestor_investimento.dto.*;
import com.colavite.gestor_investimento.service.OperacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.colavite.gestor_investimento.security.AuthenticatedUserId;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI; import java.util.List;
@RestController @RequestMapping("/operacoes") public class OperacaoController {
    private final OperacaoService service; public OperacaoController(OperacaoService service) { this.service=service; }
    @PostMapping public ResponseEntity<OperacaoResponse> cadastrar(@Valid @RequestBody OperacaoRequest request, Authentication authentication) { var response=service.cadastrar(request, AuthenticatedUserId.from(authentication)); URI location=ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri(); return ResponseEntity.created(location).body(response); }
    @GetMapping("/{id}") public ResponseEntity<OperacaoResponse> buscar(@PathVariable @Positive Long id, Authentication authentication) { return ResponseEntity.ok(service.buscarPorId(id, AuthenticatedUserId.from(authentication))); }
}
