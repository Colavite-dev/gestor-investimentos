package com.colavite.gestor_investimento.exception;

public class CvmParticipantNotAcceptedException extends RuntimeException {

    public CvmParticipantNotAcceptedException() {
        super("A instituição não é uma corretora ou distribuidora ativa perante a CVM");
    }
}
