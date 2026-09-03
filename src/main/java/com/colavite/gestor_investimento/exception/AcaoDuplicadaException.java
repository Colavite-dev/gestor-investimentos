package com.colavite.gestor_investimento.exception;

public class AcaoDuplicadaException extends RuntimeException {
    public AcaoDuplicadaException(String ticker) {
        super("Já existe uma ação cadastrada para o ticker " + ticker + " nesse mercado");
    }
}
