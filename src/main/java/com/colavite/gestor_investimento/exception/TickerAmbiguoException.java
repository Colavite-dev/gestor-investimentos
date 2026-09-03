package com.colavite.gestor_investimento.exception;

public class TickerAmbiguoException extends RuntimeException {
    public TickerAmbiguoException(String ticker) {
        super("Informe o mercado para consultar o ticker " + ticker);
    }
}
