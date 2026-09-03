package com.colavite.gestor_investimento.integration.cep;

public interface CepDataProvider {

    CepAddressData consultar(String cep);
}
