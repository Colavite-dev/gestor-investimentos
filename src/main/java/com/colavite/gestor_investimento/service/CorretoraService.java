package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.dto.CorretoraResponse;
import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.exception.CnpjDuplicadoException;
import com.colavite.gestor_investimento.exception.CorretoraNotFoundException;
import com.colavite.gestor_investimento.exception.CvmParticipantNotAcceptedException;
import com.colavite.gestor_investimento.exception.InvalidCepResponseException;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import com.colavite.gestor_investimento.integration.cep.CepDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantProvider;
import com.colavite.gestor_investimento.mapper.CorretoraMapper;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import com.colavite.gestor_investimento.validation.CnpjUtils;
import com.colavite.gestor_investimento.validation.CepUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.text.Normalizer;
import java.util.Locale;

@Service
public class CorretoraService {

    private final CorretoraRepository repository;
    private final CnpjDataProvider cnpjDataProvider;
    private final CepDataProvider cepDataProvider;
    private final CvmParticipantProvider cvmParticipantProvider;

    public CorretoraService(CorretoraRepository repository, CnpjDataProvider cnpjDataProvider,
                            CepDataProvider cepDataProvider, CvmParticipantProvider cvmParticipantProvider) {
        this.repository = repository;
        this.cnpjDataProvider = cnpjDataProvider;
        this.cepDataProvider = cepDataProvider;
        this.cvmParticipantProvider = cvmParticipantProvider;
    }

    @Transactional
    public CorretoraResponse cadastrar(CorretoraRequest request) {
        String cnpj = CnpjUtils.somenteDigitos(request.cnpj());
        if (repository.existsByCnpj(cnpj)) {
            throw new CnpjDuplicadoException(cnpj);
        }

        CnpjRegistrationData registrationData = cnpjDataProvider.consultar(cnpj);
        String cep;
        try {
            cep = CepUtils.normalizar(registrationData.cep());
        } catch (IllegalArgumentException exception) {
            throw new InvalidCepResponseException(exception);
        }
        CepAddressData cepData = cepDataProvider.consultar(cep);
        CvmParticipantData cvmParticipant = cvmParticipantProvider.consultar(cnpj)
                .orElseThrow(CvmParticipantNotAcceptedException::new);
        if (!isAcceptedByCvm(cvmParticipant)) {
            throw new CvmParticipantNotAcceptedException();
        }
        Corretora corretora = CorretoraMapper.toEntity(reconciliarEndereco(registrationData, cepData));
        corretora.marcarValidadaNaCvm();

        try {
            return CorretoraMapper.toResponse(repository.saveAndFlush(corretora));
        } catch (DataIntegrityViolationException exception) {
            throw new CnpjDuplicadoException(corretora.getCnpj());
        }
    }

    private CnpjRegistrationData reconciliarEndereco(CnpjRegistrationData cnpj, CepAddressData cep) {
        if (!sameOrBlank(cnpj.cidade(), cep.cidade()) || !sameOrBlank(cnpj.uf(), cep.uf())) {
            throw new InvalidCepResponseException();
        }
        return new CnpjRegistrationData(cnpj.cnpj(), cnpj.razaoSocial(), cnpj.nomeFantasia(), cnpj.email(),
                cnpj.telefone(), cep.cep(), firstNonBlank(cnpj.logradouro(), cep.logradouro()), cnpj.numero(),
                cnpj.complemento(), firstNonBlank(cnpj.bairro(), cep.bairro()), firstNonBlank(cnpj.cidade(), cep.cidade()),
                firstNonBlank(cnpj.uf(), cep.uf()), cnpj.situacaoCadastral());
    }

    private boolean sameOrBlank(String source, String cepValue) {
        return source == null || source.isBlank() || source.trim().equalsIgnoreCase(cepValue.trim());
    }

    private String firstNonBlank(String source, String enrichment) {
        if (source != null && !source.isBlank()) return source;
        if (enrichment == null || enrichment.isBlank()) throw new InvalidCepResponseException();
        return enrichment;
    }

    private boolean isAcceptedByCvm(CvmParticipantData participant) {
        return "ATIVO".equals(normalize(participant.situacaoRegistro()))
                && (normalize(participant.categoria()).contains("CORRETORA")
                || normalize(participant.categoria()).contains("DISTRIBUIDORA"));
    }

    private String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
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
