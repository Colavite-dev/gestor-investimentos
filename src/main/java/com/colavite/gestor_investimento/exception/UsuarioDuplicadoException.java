package com.colavite.gestor_investimento.exception;

public class UsuarioDuplicadoException extends RuntimeException {
    public UsuarioDuplicadoException() {
        super("Username ou email já está em uso");
    }
}
