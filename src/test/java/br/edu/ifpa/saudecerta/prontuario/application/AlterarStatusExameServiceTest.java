
package br.edu.ifpa.saudecerta.prontuario.application;

import java.util.Optional;

import br.edu.ifpa.saudecerta.prontuario.domain.SolicitacaoDeExame;
import br.edu.ifpa.saudecerta.prontuario.domain.SolicitacaoDeExameRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.StatusExame;
import br.edu.ifpa.saudecerta.shared.eventos.StatusExameAlterado;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AlterarStatusExameServiceTest {

    private SolicitacaoDeExameRepository exameRepository;
    private ApplicationEventPublisher eventos;
    private AlterarStatusExameService service;

    @BeforeEach
    void preparar() {
        exameRepository = mock(
            SolicitacaoDeExameRepository.class
        );

        eventos = mock(ApplicationEventPublisher.class);

        service = new AlterarStatusExameService(
            exameRepository,
            eventos
        );
    }

    @Test
    void deveMarcarExameComoRealizado() {

        when(exameRepository.buscarPorId(100L))
            .thenReturn(Optional.of(
                new SolicitacaoDeExame(100L, 200L)
            ));

        when(exameRepository.atualizarStatus(
            100L,
            StatusExame.SOLICITADO,
            StatusExame.REALIZADO
        )).thenReturn(true);

        SolicitacaoDeExame resultado = service.alterar(
            100L,
            10L,
            StatusExame.REALIZADO
        );

        assertEquals(
            StatusExame.REALIZADO,
            resultado.getStatus()
        );

        verify(eventos).publishEvent(
            argThat((StatusExameAlterado evento) ->
                evento.solicitacaoExameId().equals(100L)
                && evento.atendimentoId().equals(200L)
                && evento.usuarioId().equals(10L)
                && evento.statusAnterior().equals("SOLICITADO")
                && evento.statusNovo().equals("REALIZADO")
            )
        );
    }

    @Test
    void deveCancelarExameSolicitado() {

        when(exameRepository.buscarPorId(100L))
            .thenReturn(Optional.of(
                new SolicitacaoDeExame(100L, 200L)
            ));

        when(exameRepository.atualizarStatus(
            100L,
            StatusExame.SOLICITADO,
            StatusExame.CANCELADO
        )).thenReturn(true);

        SolicitacaoDeExame resultado = service.alterar(
            100L,
            10L,
            StatusExame.CANCELADO
        );

        assertEquals(
            StatusExame.CANCELADO,
            resultado.getStatus()
        );

        verify(eventos).publishEvent(
            any(StatusExameAlterado.class)
        );
    }

    @Test
    void naoPermiteAlterarExameFinalizado() {

        when(exameRepository.buscarPorId(100L))
            .thenReturn(Optional.of(
                new SolicitacaoDeExame(
                    100L,
                    200L,
                    StatusExame.REALIZADO
                )
            ));

        assertThrows(
            RegraDeNegocioException.class,
            () -> service.alterar(
                100L,
                10L,
                StatusExame.CANCELADO
            )
        );

        verify(exameRepository, never())
            .atualizarStatus(
                anyLong(),
                any(StatusExame.class),
                any(StatusExame.class)
            );

        verifyNoInteractions(eventos);
    }

    @Test
    void naoPermiteAlteracaoConcorrente() {

        when(exameRepository.buscarPorId(100L))
            .thenReturn(Optional.of(
                new SolicitacaoDeExame(100L, 200L)
            ));

        when(exameRepository.atualizarStatus(
            100L,
            StatusExame.SOLICITADO,
            StatusExame.REALIZADO
        )).thenReturn(false);

        assertThrows(
            RegraDeNegocioException.class,
            () -> service.alterar(
                100L,
                10L,
                StatusExame.REALIZADO
            )
        );

        verifyNoInteractions(eventos);
    }
}
