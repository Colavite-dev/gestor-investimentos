package com.colavite.gestor_investimento.exception;

public class InvalidCvmResponseException extends RuntimeException {

    public InvalidCvmResponseException() {
        super("A resposta cadastral da CVM é incompatível");
    }

    public InvalidCvmResponseException(Throwable cause) {
        super("A resposta cadastral da CVM é incompatível", cause);
    }
}
