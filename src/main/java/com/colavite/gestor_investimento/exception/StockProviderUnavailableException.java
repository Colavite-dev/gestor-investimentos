package com.colavite.gestor_investimento.exception;

public class StockProviderUnavailableException extends RuntimeException {
    public StockProviderUnavailableException() {
        super("Fonte de ações indisponível no momento");
    }

    public StockProviderUnavailableException(Throwable cause) {
        super("Fonte de ações indisponível no momento", cause);
    }
}
