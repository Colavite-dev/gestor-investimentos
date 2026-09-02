package com.colavite.gestor_investimento.exception;

public class InvalidCnpjResponseException extends RuntimeException {

    public InvalidCnpjResponseException() {
        super("A fonte cadastral retornou dados inválidos");
    }

    public InvalidCnpjResponseException(Throwable cause) {
        super("A fonte cadastral retornou dados inválidos", cause);
    }
}
