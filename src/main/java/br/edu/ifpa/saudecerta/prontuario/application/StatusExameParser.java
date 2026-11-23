
package br.edu.ifpa.saudecerta.prontuario.application;

import java.util.Locale;

import br.edu.ifpa.saudecerta.prontuario.domain.StatusExame;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;

public final class StatusExameParser {

    private StatusExameParser() {
    }

    public static StatusExame converter(String valor) {

        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(
                "Status do exame invalido."
            );
        }

        try {
            return StatusExame.valueOf(
                valor.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new RegraDeNegocioException(
                "Status do exame invalido."
            );
        }
    }
}
