package com.colavite.gestor_investimento.validation;

public final class CnpjUtils {

    private CnpjUtils() {
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }
}
