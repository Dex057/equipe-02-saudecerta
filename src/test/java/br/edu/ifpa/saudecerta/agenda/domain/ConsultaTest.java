package br.edu.ifpa.saudecerta.agenda.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ConsultaTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 1, 5);

    @Test
    void cancelarSoPermiteConsultaAgendada() {
        Consulta consulta = Consulta.agendada(1L, 2L, HOJE.atTime(8, 0));
        consulta.cancelar();
        assertEquals(StatusConsulta.CANCELADA, consulta.getStatus());

        assertThrows(RegraDeNegocioException.class, consulta::cancelar);
        Consulta realizada = new Consulta(3L, 1L, 2L, HOJE.atTime(8, 0), StatusConsulta.REALIZADA);
        assertThrows(RegraDeNegocioException.class, realizada::cancelar);
    }

    @Test
    void marcarRealizadaRejeitaDataFuturaEEstadosNaoAgendados() {
        Consulta futura = Consulta.agendada(1L, 2L, LocalDateTime.of(2026, 1, 6, 8, 0));
        assertThrows(RegraDeNegocioException.class, () -> futura.marcarRealizada(HOJE));

        Consulta consulta = Consulta.agendada(1L, 2L, HOJE.atTime(8, 0));
        consulta.marcarRealizada(HOJE);
        assertEquals(StatusConsulta.REALIZADA, consulta.getStatus());
        assertThrows(RegraDeNegocioException.class, () -> consulta.marcarRealizada(HOJE));
    }

    @Test
    void disponibilidadeAceitaSomenteSlotsUteisDaGrade() {
        ValidadorDeDisponibilidade validador = new ValidadorDeDisponibilidade();
        validador.validar(HOJE.atTime(8, 0));
        validador.validar(HOJE.atTime(16, 30));

        assertThrows(RegraDeNegocioException.class, () -> validador.validar(HOJE.atTime(8, 15)));
        assertThrows(RegraDeNegocioException.class,
                () -> validador.validar(LocalDate.of(2026, 1, 10).atTime(8, 0)));
        assertEquals(18, validador.gerarHorarios(HOJE).size());
    }
}
