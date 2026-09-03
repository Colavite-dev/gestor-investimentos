package com.colavite.gestor_investimento.exception;

public class StockTickerNotFoundException extends RuntimeException {
    public StockTickerNotFoundException() {
        super("Ticker não encontrado ou incompatível com o mercado informado");
    }
}
