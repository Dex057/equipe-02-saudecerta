
package br.edu.ifpa.saudecerta.prontuario.domain;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolicitacaoDeExameTest {

    @Test
    void exameNovoComecaSolicitado() {
        SolicitacaoDeExame exame = new SolicitacaoDeExame(1L, 10L);

        assertEquals(StatusExame.SOLICITADO, exame.getStatus());
        assertEquals(10L, exame.getAtendimentoId());
    }

    @Test
    void podeMarcarExameComoRealizado() {
        SolicitacaoDeExame exame = new SolicitacaoDeExame(1L, 10L);

        exame.marcarRealizado();

        assertEquals(StatusExame.REALIZADO, exame.getStatus());
    }

    @Test
    void podeCancelarExameSolicitado() {
        SolicitacaoDeExame exame = new SolicitacaoDeExame(1L, 10L);

        exame.cancelar();

        assertEquals(StatusExame.CANCELADO, exame.getStatus());
    }

    @Test
    void naoPodeCancelarExameRealizado() {
        SolicitacaoDeExame exame = new SolicitacaoDeExame(1L, 10L);
        exame.marcarRealizado();

        assertThrows(RegraDeNegocioException.class, exame::cancelar);
        assertEquals(StatusExame.REALIZADO, exame.getStatus());
    }

    @Test
    void naoPodeRealizarExameCancelado() {
        SolicitacaoDeExame exame = new SolicitacaoDeExame(1L, 10L);
        exame.cancelar();

        assertThrows(RegraDeNegocioException.class, exame::marcarRealizado);
        assertEquals(StatusExame.CANCELADO, exame.getStatus());
    }

    @Test
    void examePrecisaDeAtendimentoValido() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new SolicitacaoDeExame(1L, null)
        );
    }
}
