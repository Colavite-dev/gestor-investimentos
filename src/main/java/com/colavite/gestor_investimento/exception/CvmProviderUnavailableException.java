package com.colavite.gestor_investimento.exception;

public class CvmProviderUnavailableException extends RuntimeException {

    public CvmProviderUnavailableException() {
        super("O cadastro da CVM está indisponível no momento");
    }

    public CvmProviderUnavailableException(Throwable cause) {
        super("O cadastro da CVM está indisponível no momento", cause);
    }
}
