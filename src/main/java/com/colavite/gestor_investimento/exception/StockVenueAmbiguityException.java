package com.colavite.gestor_investimento.exception;

public class StockVenueAmbiguityException extends RuntimeException {
    public StockVenueAmbiguityException() {
        super("Ticker possui múltiplos venues elegíveis e não foi possível determinar o mercado principal");
    }
}
