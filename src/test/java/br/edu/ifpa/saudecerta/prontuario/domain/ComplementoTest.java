
package br.edu.ifpa.saudecerta.prontuario.domain;

import java.lang.reflect.Modifier;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComplementoTest {

    private Complemento criarComplemento() {
        return new Complemento(
            1L,
            10L,
            20L,
            "Paciente apresentou melhora.",
            Instant.parse("2026-11-19T16:00:00Z")
        );
    }

    @Test
    void deveCriarComplementoValido() {
        Complemento complemento = criarComplemento();

        assertEquals(10L, complemento.getAtendimentoId());
        assertEquals(20L, complemento.getProfissionalId());
        assertEquals(
            "Paciente apresentou melhora.",
            complemento.getTexto()
        );
    }

    @Test
    void naoPermiteComplementoVazio() {
        assertThrows(IllegalArgumentException.class,
            () -> new Complemento(
                1L, 10L, 20L, " ", Instant.now()
            )
        );
    }

    @Test
    void exigeAtendimentoValido() {
        assertThrows(IllegalArgumentException.class,
            () -> new Complemento(
                1L, null, 20L, "Observação", Instant.now()
            )
        );
    }

    @Test
    void exigeProfissionalValido() {
        assertThrows(IllegalArgumentException.class,
            () -> new Complemento(
                1L, 10L, 0L, "Observação", Instant.now()
            )
        );
    }

    @Test
    void camposSaoImutaveis() {
        for (var campo : Complemento.class.getDeclaredFields()) {
            assertTrue(
                Modifier.isFinal(campo.getModifiers()),
                "O campo " + campo.getName() + " deve ser final"
            );
        }
    }
}
