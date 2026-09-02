package com.colavite.gestor_investimento.exception;

public class CorretoraNotFoundException extends RuntimeException {

    private CorretoraNotFoundException(String message) {
        super(message);
    }

    public static CorretoraNotFoundException porId(Long id) {
        return new CorretoraNotFoundException("Corretora não encontrada para o ID " + id);
    }

    public static CorretoraNotFoundException porCnpj(String cnpj) {
        return new CorretoraNotFoundException("Corretora não encontrada para o CNPJ " + cnpj);
    }
}
