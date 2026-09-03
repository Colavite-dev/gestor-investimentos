package com.colavite.gestor_investimento.exception;
public class OperacaoNotFoundException extends RuntimeException { private OperacaoNotFoundException(String message) { super(message); } public static OperacaoNotFoundException porId(Long id) { return new OperacaoNotFoundException("Operação não encontrada: " + id); } }
