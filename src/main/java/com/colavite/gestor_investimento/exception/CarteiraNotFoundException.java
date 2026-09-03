package com.colavite.gestor_investimento.exception;
public class CarteiraNotFoundException extends RuntimeException {
    private CarteiraNotFoundException(String message) { super(message); }
    public static CarteiraNotFoundException porId(Long id) { return new CarteiraNotFoundException("Carteira não encontrada: " + id); }
}
