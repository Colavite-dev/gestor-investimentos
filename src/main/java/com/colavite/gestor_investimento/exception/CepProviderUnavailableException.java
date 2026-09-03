package com.colavite.gestor_investimento.exception;

public class CepProviderUnavailableException extends RuntimeException {
    public CepProviderUnavailableException() {
        super("A fonte de endereço está temporariamente indisponível");
    }

    public CepProviderUnavailableException(Throwable cause) {
        super("A fonte de endereço está temporariamente indisponível", cause);
    }
}
