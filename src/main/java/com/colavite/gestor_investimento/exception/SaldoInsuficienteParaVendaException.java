package com.colavite.gestor_investimento.exception;

public class SaldoInsuficienteParaVendaException extends RuntimeException {

    public SaldoInsuficienteParaVendaException() {
        super("Saldo insuficiente para realizar a venda");
    }
}
