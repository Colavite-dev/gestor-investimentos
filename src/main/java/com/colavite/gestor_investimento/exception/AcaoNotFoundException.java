package com.colavite.gestor_investimento.exception;

public class AcaoNotFoundException extends RuntimeException {
    private AcaoNotFoundException(String message) {
        super(message);
    }

    public static AcaoNotFoundException porId(Long id) {
        return new AcaoNotFoundException("Ação não encontrada para o id " + id);
    }

    public static AcaoNotFoundException porTicker(String ticker) {
        return new AcaoNotFoundException("Ação não encontrada para o ticker " + ticker);
    }
}
