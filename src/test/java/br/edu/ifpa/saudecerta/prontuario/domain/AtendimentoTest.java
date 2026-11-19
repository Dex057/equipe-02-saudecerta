
package br.edu.ifpa.saudecerta.prontuario.domain;

import java.time.Instant;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtendimentoTest {

    private Atendimento criarAtendimento() {
        return new Atendimento(
            1L,
            10L,
            20L,
            "Dor de cabeça",
            "Paciente consciente",
            "Prescrito repouso",
            Instant.parse("2026-11-19T15:00:00Z")
        );
    }

    @Test
    void deveCriarAtendimentoValido() {
        Atendimento atendimento = criarAtendimento();

        assertEquals(10L, atendimento.getProntuarioId());
        assertEquals(20L, atendimento.getConsultaId());
        assertEquals("Dor de cabeça", atendimento.getQueixa());
        assertEquals("Paciente consciente", atendimento.getEvolucao());
        assertEquals("Prescrito repouso", atendimento.getConduta());
    }

    @Test
    void naoPermiteProntuarioInvalido() {
        assertThrows(IllegalArgumentException.class,
            () -> new Atendimento(
                1L, null, 20L, "Queixa",
                "Evolução", "Conduta", Instant.now()
            )
        );
    }

    @Test
    void naoPermiteConsultaInvalida() {
        assertThrows(IllegalArgumentException.class,
            () -> new Atendimento(
                1L, 10L, 0L, "Queixa",
                "Evolução", "Conduta", Instant.now()
            )
        );
    }

    @Test
    void naoPermiteQueixaVazia() {
        assertThrows(IllegalArgumentException.class,
            () -> new Atendimento(
                1L, 10L, 20L, " ",
                "Evolução", "Conduta", Instant.now()
            )
        );
    }

    @Test
    void camposDoAtendimentoSaoFinais() {
        for (var campo : Atendimento.class.getDeclaredFields()) {
            assertTrue(
                Modifier.isFinal(campo.getModifiers()),
                "O campo " + campo.getName() + " deve ser final"
            );
        }
    }
}
