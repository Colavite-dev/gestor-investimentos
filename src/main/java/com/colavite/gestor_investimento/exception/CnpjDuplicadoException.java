package com.colavite.gestor_investimento.exception;

public class CnpjDuplicadoException extends RuntimeException {

    public CnpjDuplicadoException(String cnpj) {
        super("Já existe uma corretora cadastrada com o CNPJ " + cnpj);
    }
}
