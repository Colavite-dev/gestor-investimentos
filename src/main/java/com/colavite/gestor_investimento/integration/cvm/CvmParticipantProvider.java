package com.colavite.gestor_investimento.integration.cvm;

import java.util.Optional;

public interface CvmParticipantProvider {

    Optional<CvmParticipantData> consultar(String cnpj);
}
