package com.colavite.gestor_investimento.exception;
public class CarteiraDuplicadaException extends RuntimeException {
    public CarteiraDuplicadaException(String nome) { super("Carteira já cadastrada: " + nome); }
}
