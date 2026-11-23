
package br.edu.ifpa.saudecerta.prontuario.application;

import org.junit.jupiter.api.Test;

import br.edu.ifpa.saudecerta.prontuario.domain.StatusExame;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;

import static org.junit.jupiter.api.Assertions.*;

class StatusExameParserTest {

    @Test
    void converteStatusValido() {
        assertEquals(
            StatusExame.REALIZADO,
            StatusExameParser.converter("REALIZADO")
        );
    }

    @Test
    void aceitaLetrasMinusculas() {
        assertEquals(
            StatusExame.CANCELADO,
            StatusExameParser.converter("cancelado")
        );
    }

    @Test
    void rejeitaStatusInvalido() {
        assertThrows(
            RegraDeNegocioException.class,
            () -> StatusExameParser.converter("FINALIZADO")
        );

        assertThrows(
            RegraDeNegocioException.class,
            () -> StatusExameParser.converter(null)
        );
    }
}
