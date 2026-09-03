package com.colavite.gestor_investimento.entity;

public enum Mercado {
    BRASIL(Moeda.BRL),
    ESTADOS_UNIDOS(Moeda.USD);

    private final Moeda moeda;

    Mercado(Moeda moeda) {
        this.moeda = moeda;
    }

    public Moeda moeda() {
        return moeda;
    }
}
