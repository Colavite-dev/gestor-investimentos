package com.colavite.gestor_investimento.exception;

public class InvalidQuoteHistoryRangeException extends RuntimeException {
    public InvalidQuoteHistoryRangeException() { super("Intervalo de histórico de cotações inválido"); }
}
