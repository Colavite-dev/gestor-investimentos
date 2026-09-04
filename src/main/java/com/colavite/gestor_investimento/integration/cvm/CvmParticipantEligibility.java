package com.colavite.gestor_investimento.integration.cvm;

import java.text.Normalizer;
import java.util.Locale;

public final class CvmParticipantEligibility {

    private CvmParticipantEligibility() {
    }

    public static boolean isEligible(CvmParticipantData participant) {
        return participant != null
                && "ATIVO".equals(normalize(participant.situacaoRegistro()))
                && (normalize(participant.categoria()).contains("CORRETORA")
                || normalize(participant.categoria()).contains("DISTRIBUIDORA"));
    }

    public static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}
