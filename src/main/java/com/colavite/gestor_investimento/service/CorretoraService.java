package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.dto.CorretoraResponse;
import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.exception.CnpjDuplicadoException;
import com.colavite.gestor_investimento.exception.CorretoraNotFoundException;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.mapper.CorretoraMapper;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import com.colavite.gestor_investimento.validation.CnpjUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CorretoraService {

    private final CorretoraRepository repository;
    private final CnpjDataProvider cnpjDataProvider;

    public CorretoraService(CorretoraRepository repository, CnpjDataProvider cnpjDataProvider) {
        this.repository = repository;
        this.cnpjDataProvider = cnpjDataProvider;
    }

    @Transactional
    public CorretoraResponse cadastrar(CorretoraRequest request) {
        String cnpj = CnpjUtils.somenteDigitos(request.cnpj());
        if (repository.existsByCnpj(cnpj)) {
            throw new CnpjDuplicadoException(cnpj);
        }

        CnpjRegistrationData registrationData = cnpjDataProvider.consultar(cnpj);
        Corretora corretora = CorretoraMapper.toEntity(registrationData);

        try {
            return CorretoraMapper.toResponse(repository.saveAndFlush(corretora));
        } catch (DataIntegrityViolationException exception) {
            throw new CnpjDuplicadoException(corretora.getCnpj());
        }
    }

    @Transactional(readOnly = true)
    public List<CorretoraResponse> listar() {
        return repository.findAllByOrderByIdAsc().stream()
                .map(CorretoraMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CorretoraResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(CorretoraMapper::toResponse)
                .orElseThrow(() -> CorretoraNotFoundException.porId(id));
    }

    @Transactional(readOnly = true)
    public CorretoraResponse buscarPorCnpj(String cnpj) {
        String normalizado = CnpjUtils.somenteDigitos(cnpj);
        return repository.findByCnpj(normalizado)
                .map(CorretoraMapper::toResponse)
                .orElseThrow(() -> CorretoraNotFoundException.porCnpj(normalizado));
    }
}
