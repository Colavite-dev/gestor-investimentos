package com.colavite.gestor_investimento.exception;

public class CepNotFoundException extends RuntimeException {
    public CepNotFoundException() {
        super("CEP não encontrado na fonte de endereço");
    }
}
