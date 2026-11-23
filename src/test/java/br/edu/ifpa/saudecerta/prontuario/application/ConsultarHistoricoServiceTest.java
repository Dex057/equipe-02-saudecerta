
package br.edu.ifpa.saudecerta.prontuario.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import br.edu.ifpa.saudecerta.prontuario.domain.Atendimento;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.ProntuarioRepository;
import br.edu.ifpa.saudecerta.shared.eventos.ProntuarioAcessado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ConsultarHistoricoServiceTest {

    private ProntuarioRepository prontuarioRepository;
    private AtendimentoRepository atendimentoRepository;
    private ApplicationEventPublisher eventos;
    private ConsultarHistoricoService service;

    @BeforeEach
    void preparar() {
        prontuarioRepository = mock(ProntuarioRepository.class);
        atendimentoRepository = mock(AtendimentoRepository.class);
        eventos = mock(ApplicationEventPublisher.class);

        service = new ConsultarHistoricoService(
            prontuarioRepository,
            atendimentoRepository,
            eventos
        );
    }

    @Test
    void deveConsultarHistoricoPaginado() {

        Atendimento atendimento = new Atendimento(
            100L,
            20L,
            800L,
            "Dor abdominal",
            "Paciente consciente",
            "Solicitar exames",
            Instant.parse("2026-11-21T10:00:00Z")
        );

        when(prontuarioRepository.buscarIdPorPaciente(300L))
            .thenReturn(Optional.of(20L));

        when(atendimentoRepository.buscarPaginaPorProntuario(
            20L, 0, 10
        )).thenReturn(List.of(atendimento));

        when(atendimentoRepository.contarPorProntuario(20L))
            .thenReturn(1L);

        var resultado = service.consultar(
            300L,
            10L,
            0
        );

        assertEquals(300L, resultado.pacienteId());
        assertEquals(0, resultado.pagina());
        assertEquals(10, resultado.tamanho());
        assertEquals(1L, resultado.total());
        assertEquals(1, resultado.atendimentos().size());

        assertEquals(
            "Dor abdominal",
            resultado.atendimentos().get(0).getQueixa()
        );

        verify(eventos).publishEvent(
            argThat((ProntuarioAcessado evento) ->
                evento.pacienteId().equals(300L)
                && evento.usuarioId().equals(10L)
                && evento.dataHora() != null
            )
        );
    }

    @Test
    void deveRetornarHistoricoVazioSemCriarProntuario() {

        when(prontuarioRepository.buscarIdPorPaciente(301L))
            .thenReturn(Optional.empty());

        var resultado = service.consultar(
            301L,
            10L,
            0
        );

        assertEquals(0L, resultado.total());
        assertTrue(resultado.atendimentos().isEmpty());

        verify(prontuarioRepository, never())
            .buscarOuCriarPorPaciente(anyLong());

        verifyNoInteractions(atendimentoRepository);

        verify(eventos).publishEvent(
            any(ProntuarioAcessado.class)
        );
    }

    @Test
    void naoPermitePaginaInvalida() {

        assertThrows(
            IllegalArgumentException.class,
            () -> service.consultar(
                300L,
                10L,
                -1
            )
        );

        verifyNoInteractions(
            prontuarioRepository,
            atendimentoRepository,
            eventos
        );
    }
}
