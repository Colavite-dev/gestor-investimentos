package com.colavite.gestor_investimento.integration.cnpj;

public interface CnpjDataProvider {

    CnpjRegistrationData consultar(String cnpj);
}
