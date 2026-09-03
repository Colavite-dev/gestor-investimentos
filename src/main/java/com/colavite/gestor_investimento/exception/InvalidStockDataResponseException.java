package com.colavite.gestor_investimento.exception;

public class InvalidStockDataResponseException extends RuntimeException {
    public InvalidStockDataResponseException() {
        super("Resposta inválida da fonte de ações");
    }

    public InvalidStockDataResponseException(Throwable cause) {
        super("Resposta inválida da fonte de ações", cause);
    }
}
