package com.colavite.gestor_investimento.exception;

public class CnpjNotFoundException extends RuntimeException {

    public CnpjNotFoundException(String cnpj) {
        super("CNPJ não encontrado na fonte cadastral: " + cnpj);
    }
}
