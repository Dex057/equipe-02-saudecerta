
package br.edu.ifpa.saudecerta.prontuario.application;

import java.time.LocalDateTime;
import java.util.Optional;

import br.edu.ifpa.saudecerta.agenda.AgendaApi;
import br.edu.ifpa.saudecerta.agenda.ConsultaResumo;
import br.edu.ifpa.saudecerta.prontuario.domain.Atendimento;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.ProntuarioRepository;
import br.edu.ifpa.saudecerta.shared.eventos.AtendimentoRegistrado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistrarAtendimentoServiceTest {

    private AgendaApi agendaApi;
    private ProntuarioRepository prontuarioRepository;
    private AtendimentoRepository atendimentoRepository;
    private ApplicationEventPublisher eventos;

    private RegistrarAtendimentoService service;

    @BeforeEach
    void preparar() {
        agendaApi = mock(AgendaApi.class);
        prontuarioRepository = mock(ProntuarioRepository.class);
        atendimentoRepository = mock(AtendimentoRepository.class);
        eventos = mock(ApplicationEventPublisher.class);

        service = new RegistrarAtendimentoService(
            agendaApi,
            prontuarioRepository,
            atendimentoRepository,
            eventos
        );
    }

    private ConsultaResumo criarConsulta() {
        return new ConsultaResumo(
            800L,
            100L,
            50L,
            LocalDateTime.of(2026, 11, 19, 10, 0),
            "AGENDADA"
        );
    }

    @Test
    void deveRegistrarAtendimentoEPublicarEvento() {

        when(agendaApi.buscarConsulta(800L))
            .thenReturn(Optional.of(criarConsulta()));

        when(prontuarioRepository.buscarOuCriarPorPaciente(100L))
            .thenReturn(20L);

        when(atendimentoRepository.inserir(any(Atendimento.class)))
            .thenAnswer(invocation -> {
                Atendimento recebido = invocation.getArgument(0);

                return new Atendimento(
                    300L,
                    recebido.getProntuarioId(),
                    recebido.getConsultaId(),
                    recebido.getQueixa(),
                    recebido.getEvolucao(),
                    recebido.getConduta(),
                    recebido.getDataHora()
                );
            });

        Atendimento salvo = service.registrar(
            800L,
            10L,
            "Dor abdominal",
            "Paciente consciente",
            "Solicitar exames"
        );

        assertEquals(300L, salvo.getId());
        assertEquals(20L, salvo.getProntuarioId());
        assertEquals(800L, salvo.getConsultaId());

        verify(agendaApi).marcarRealizada(800L);

        verify(eventos).publishEvent(
            argThat((AtendimentoRegistrado evento) ->
                evento.atendimentoId().equals(300L)
                && evento.pacienteId().equals(100L)
                && evento.usuarioId().equals(10L)
                && evento.tipo().equals("ATENDIMENTO")
            )
        );
    }

    @Test
    void naoPermiteRegistrarConsultaInexistente() {

        when(agendaApi.buscarConsulta(999L))
            .thenReturn(Optional.empty());

        assertThrows(
            java.util.NoSuchElementException.class,
            () -> service.registrar(
                999L,
                10L,
                "Dor abdominal",
                "Paciente consciente",
                "Solicitar exames"
            )
        );

        verify(agendaApi, never())
            .marcarRealizada(anyLong());

        verifyNoInteractions(
            prontuarioRepository,
            atendimentoRepository,
            eventos
        );
    }

    @Test
    void naoRegistraQuandoAgendaRejeitaConsulta() {

        when(agendaApi.buscarConsulta(800L))
            .thenReturn(Optional.of(criarConsulta()));

        doThrow(new IllegalStateException(
            "Consulta nao pode ser realizada."
        ))
        .when(agendaApi).marcarRealizada(800L);

        assertThrows(
            IllegalStateException.class,
            () -> service.registrar(
                800L,
                10L,
                "Dor abdominal",
                "Paciente consciente",
                "Solicitar exames"
            )
        );

        verifyNoInteractions(
            prontuarioRepository,
            atendimentoRepository,
            eventos
        );
    }
}
