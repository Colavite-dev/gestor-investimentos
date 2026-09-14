package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.*;
import com.colavite.gestor_investimento.entity.Carteira;
import com.colavite.gestor_investimento.exception.*;
import com.colavite.gestor_investimento.mapper.CarteiraMapper;
import com.colavite.gestor_investimento.repository.CarteiraRepository;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.util.*;

@Service
public class CarteiraService {
    private final CarteiraRepository repository;
    private final UsuarioRepository usuarios;
    public CarteiraService(CarteiraRepository repository, UsuarioRepository usuarios) { this.repository = repository; this.usuarios = usuarios; }
    @Transactional
    public CarteiraResponse cadastrar(CarteiraRequest request, Long usuarioId) {
        String nome = request.nome().trim();
        String normalizado = normalizar(nome);
        if (repository.existsByUsuarioIdAndNomeNormalizado(usuarioId, normalizado)) throw new CarteiraDuplicadaException(nome);
        var usuario = usuarios.findById(usuarioId).orElseThrow(CredenciaisInvalidasException::new);
        try { return CarteiraMapper.toResponse(repository.saveAndFlush(new Carteira(nome, normalizado, optional(request.descricao()), usuario))); }
        catch (DataIntegrityViolationException e) { throw new CarteiraDuplicadaException(nome); }
    }
    @Transactional(readOnly = true) public List<CarteiraResponse> listar(Long usuarioId) { return repository.findAllByUsuarioIdOrderByIdAsc(usuarioId).stream().map(CarteiraMapper::toResponse).toList(); }
    @Transactional(readOnly = true) public CarteiraResponse buscarPorId(Long id, Long usuarioId) { return repository.findByIdAndUsuarioId(id, usuarioId).map(CarteiraMapper::toResponse).orElseThrow(() -> CarteiraNotFoundException.porId(id)); }
    private String optional(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String normalizar(String value) { return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT); }
}
