package com.colavite.gestor_investimento.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<ValidCnpj, String> {

    private static final String FORMATO = "(?:\\d{14}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})";
    private static final int[] PESOS_PRIMEIRO_DIGITO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO_DIGITO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || !valor.matches(FORMATO)) {
            return false;
        }

        String cnpj = CnpjUtils.somenteDigitos(valor);
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        int primeiro = calcularDigito(cnpj.substring(0, 12), PESOS_PRIMEIRO_DIGITO);
        int segundo = calcularDigito(cnpj.substring(0, 12) + primeiro, PESOS_SEGUNDO_DIGITO);
        return cnpj.endsWith(Integer.toString(primeiro) + segundo);
    }

    private int calcularDigito(String base, int[] pesos) {
        int soma = 0;
        for (int indice = 0; indice < pesos.length; indice++) {
            soma += Character.digit(base.charAt(indice), 10) * pesos[indice];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
