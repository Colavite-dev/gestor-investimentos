package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.*;
import com.colavite.gestor_investimento.entity.Carteira;
import com.colavite.gestor_investimento.exception.*;
import com.colavite.gestor_investimento.mapper.CarteiraMapper;
import com.colavite.gestor_investimento.repository.CarteiraRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.util.*;

@Service
public class CarteiraService {
    private final CarteiraRepository repository;
    public CarteiraService(CarteiraRepository repository) { this.repository = repository; }
    @Transactional
    public CarteiraResponse cadastrar(CarteiraRequest request) {
        String nome = request.nome().trim();
        String normalizado = normalizar(nome);
        if (repository.existsByNomeNormalizado(normalizado)) throw new CarteiraDuplicadaException(nome);
        try { return CarteiraMapper.toResponse(repository.saveAndFlush(new Carteira(nome, normalizado, optional(request.descricao())))); }
        catch (DataIntegrityViolationException e) { throw new CarteiraDuplicadaException(nome); }
    }
    @Transactional(readOnly = true) public List<CarteiraResponse> listar() { return repository.findAllByOrderByIdAsc().stream().map(CarteiraMapper::toResponse).toList(); }
    @Transactional(readOnly = true) public CarteiraResponse buscarPorId(Long id) { return repository.findById(id).map(CarteiraMapper::toResponse).orElseThrow(() -> CarteiraNotFoundException.porId(id)); }
    private String optional(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String normalizar(String value) { return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT); }
}
