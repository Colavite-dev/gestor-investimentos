package com.colavite.gestor_investimento.exception;

public class CnpjProviderUnavailableException extends RuntimeException {

    public CnpjProviderUnavailableException() {
        super("A fonte cadastral está temporariamente indisponível");
    }

    public CnpjProviderUnavailableException(Throwable cause) {
        super("A fonte cadastral está temporariamente indisponível", cause);
    }
}
