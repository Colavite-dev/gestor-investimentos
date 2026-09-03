package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CotacaoHistoricaResponse;
import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.CotacaoHistorica;
import com.colavite.gestor_investimento.exception.AcaoNotFoundException;
import com.colavite.gestor_investimento.exception.InvalidQuoteHistoryRangeException;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import com.colavite.gestor_investimento.repository.CotacaoHistoricaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class CotacaoHistoricaService {
    private final AcaoRepository acoes;
    private final CotacaoHistoricaRepository historico;

    public CotacaoHistoricaService(AcaoRepository acoes, CotacaoHistoricaRepository historico) {
        this.acoes = acoes;
        this.historico = historico;
    }

    @Transactional
    public void registrar(Acao acao, java.math.BigDecimal cotacao, Instant dataHoraCotacao) {
        historico.saveAndFlush(new CotacaoHistorica(acao, cotacao, dataHoraCotacao, Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<CotacaoHistoricaResponse> listar(Long acaoId, Instant de, Instant ate) {
        if (!acoes.existsById(acaoId)) throw AcaoNotFoundException.porId(acaoId);
        if (de != null && ate != null && de.isAfter(ate)) throw new InvalidQuoteHistoryRangeException();
        List<CotacaoHistorica> dados;
        if (de != null && ate != null) dados = historico.findByAcaoIdAndDataHoraCotacaoGreaterThanEqualAndDataHoraCotacaoLessThanEqualOrderByDataHoraCotacaoAscIdAsc(acaoId, de, ate);
        else if (de != null) dados = historico.findByAcaoIdAndDataHoraCotacaoGreaterThanEqualOrderByDataHoraCotacaoAscIdAsc(acaoId, de);
        else if (ate != null) dados = historico.findByAcaoIdAndDataHoraCotacaoLessThanEqualOrderByDataHoraCotacaoAscIdAsc(acaoId, ate);
        else dados = historico.findByAcaoIdOrderByDataHoraCotacaoAscIdAsc(acaoId);
        return dados.stream().map(h -> new CotacaoHistoricaResponse(h.getId(), h.getCotacao(), h.getDataHoraCotacao(), h.getDataRegistro())).toList();
    }
}
