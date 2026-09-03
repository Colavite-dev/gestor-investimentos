package com.colavite.gestor_investimento.validation;

public final class CepUtils {

    private CepUtils() {
    }

    public static String normalizar(String valor) {
        if (valor == null || valor.isBlank() || !valor.replaceAll("[-\\s]", "").matches("\\d+")) {
            throw new IllegalArgumentException("CEP inválido");
        }
        String cep = valor.replaceAll("\\D", "");
        if (cep.length() != 8) {
            throw new IllegalArgumentException("CEP inválido");
        }
        return cep;
    }
}
