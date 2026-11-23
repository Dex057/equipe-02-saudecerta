
package br.edu.ifpa.saudecerta.prontuario.application;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import br.edu.ifpa.saudecerta.prontuario.domain.Atendimento;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.Complemento;
import br.edu.ifpa.saudecerta.prontuario.domain.ComplementoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.ProntuarioRepository;
import br.edu.ifpa.saudecerta.shared.eventos.AtendimentoRegistrado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ComplementarAtendimentoServiceTest {

    private AtendimentoRepository atendimentoRepository;
    private ComplementoRepository complementoRepository;
    private ProntuarioRepository prontuarioRepository;
    private ApplicationEventPublisher eventos;

    private ComplementarAtendimentoService service;

    @BeforeEach
    void preparar() {
        atendimentoRepository = mock(AtendimentoRepository.class);
        complementoRepository = mock(ComplementoRepository.class);
        prontuarioRepository = mock(ProntuarioRepository.class);
        eventos = mock(ApplicationEventPublisher.class);

        service = new ComplementarAtendimentoService(
            atendimentoRepository,
            complementoRepository,
            prontuarioRepository,
            eventos
        );
    }

    private Atendimento criarAtendimento() {
        return new Atendimento(
            100L,
            20L,
            800L,
            "Dor abdominal",
            "Paciente consciente",
            "Solicitar exames",
            Instant.parse("2026-11-21T10:00:00Z")
        );
    }

    @Test
    void deveComplementarAtendimentoEPublicarEvento() {

        when(atendimentoRepository.buscarPorId(100L))
            .thenReturn(Optional.of(criarAtendimento()));

        when(prontuarioRepository.buscarPacienteIdPorId(20L))
            .thenReturn(Optional.of(300L));

        when(complementoRepository.inserir(any(Complemento.class)))
            .thenAnswer(invocation -> {
                Complemento recebido = invocation.getArgument(0);

                return new Complemento(
                    500L,
                    recebido.getAtendimentoId(),
                    recebido.getProfissionalId(),
                    recebido.getTexto(),
                    recebido.getDataHora()
                );
            });

        Complemento salvo = service.complementar(
            100L,
            50L,
            10L,
            "Paciente apresentou melhora."
        );

        assertEquals(500L, salvo.getId());
        assertEquals(100L, salvo.getAtendimentoId());
        assertEquals(50L, salvo.getProfissionalId());
        assertEquals(
            "Paciente apresentou melhora.",
            salvo.getTexto()
        );

        verify(complementoRepository).inserir(
            any(Complemento.class)
        );

        verify(eventos).publishEvent(
            argThat((AtendimentoRegistrado evento) ->
                evento.atendimentoId().equals(100L)
                && evento.pacienteId().equals(300L)
                && evento.usuarioId().equals(10L)
                && evento.tipo().equals("COMPLEMENTO")
            )
        );
    }

    @Test
    void naoPermiteComplementarAtendimentoInexistente() {

        when(atendimentoRepository.buscarPorId(999L))
            .thenReturn(Optional.empty());

        assertThrows(
            NoSuchElementException.class,
            () -> service.complementar(
                999L,
                50L,
                10L,
                "Complemento de teste"
            )
        );

        verifyNoInteractions(
            complementoRepository,
            prontuarioRepository,
            eventos
        );
    }

    @Test
    void naoPermiteComplementarSemProntuario() {

        when(atendimentoRepository.buscarPorId(100L))
            .thenReturn(Optional.of(criarAtendimento()));

        when(prontuarioRepository.buscarPacienteIdPorId(20L))
            .thenReturn(Optional.empty());

        assertThrows(
            NoSuchElementException.class,
            () -> service.complementar(
                100L,
                50L,
                10L,
                "Complemento de teste"
            )
        );

        verifyNoInteractions(
            complementoRepository,
            eventos
        );
    }
}
