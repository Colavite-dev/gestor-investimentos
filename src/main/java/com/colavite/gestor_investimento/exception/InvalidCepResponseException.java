package com.colavite.gestor_investimento.exception;

public class InvalidCepResponseException extends RuntimeException {
    public InvalidCepResponseException() {
        super("A fonte de endereço retornou dados inválidos");
    }

    public InvalidCepResponseException(Throwable cause) {
        super("A fonte de endereço retornou dados inválidos", cause);
    }
}
