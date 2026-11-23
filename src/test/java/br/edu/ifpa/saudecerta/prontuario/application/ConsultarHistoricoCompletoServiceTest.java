package br.edu.ifpa.saudecerta.prontuario.application;

import java.time.Instant;
import java.util.List;

import br.edu.ifpa.saudecerta.prontuario.domain.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultarHistoricoCompletoServiceTest {

    @Test
    void devolveAtendimentoComComplementosPrescricoesEExames() {
        var historico = mock(ConsultarHistoricoService.class);
        var complementos = mock(ComplementoRepository.class);
        var prescricoes = mock(PrescricaoLeituraRepository.class);
        var exames = mock(SolicitacaoDeExameRepository.class);

        Instant hora = Instant.parse("2026-11-21T10:00:00Z");
        var atendimento = new Atendimento(100L, 20L, 800L, "Dor",
                "Consciente", "Solicitar exames", hora);
        when(historico.consultar(300L, 9L, 0)).thenReturn(
            new ConsultarHistoricoService.PaginaHistorico(
                300L, 0, 10, 1L, List.of(atendimento)));
        when(complementos.buscarPorAtendimento(100L)).thenReturn(
            List.of(new Complemento(500L, 100L, 7L, "Melhora", hora)));
        when(prescricoes.buscarPorAtendimento(100L)).thenReturn(
            List.of(new Prescricao(600L, 100L,
                List.of(new ItemDePrescricao(700L, "Dipirona", "500mg", "8/8h", "3 dias")),
                hora)));
        when(exames.buscarPorAtendimento(100L)).thenReturn(
            List.of(new SolicitacaoDeExame(900L, 100L, StatusExame.REALIZADO)));

        var service = new ConsultarHistoricoCompletoService(
            historico, complementos, prescricoes, exames);
        var resposta = service.consultarResumo(300L, 9L, 0);

        assertEquals(1L, resposta.total());
        var item = resposta.atendimentos().get(0);
        assertEquals("Melhora", item.complementos().get(0).texto());
        assertEquals("Dipirona", item.prescricoes().get(0).itens().get(0).medicamento());
        assertEquals("REALIZADO", item.exames().get(0).status());
        verify(historico).consultar(300L, 9L, 0);
    }

    @Test
    void retornaVazioSemConsultarRegistrosClinicos() {
        var historico = mock(ConsultarHistoricoService.class);
        var complementos = mock(ComplementoRepository.class);
        var prescricoes = mock(PrescricaoLeituraRepository.class);
        var exames = mock(SolicitacaoDeExameRepository.class);

        when(historico.consultar(301L, 9L, 0)).thenReturn(
            new ConsultarHistoricoService.PaginaHistorico(
                301L, 0, 10, 0L, List.of()));

        var service = new ConsultarHistoricoCompletoService(
            historico, complementos, prescricoes, exames);
        var resposta = service.consultarResumo(301L, 9L, 0);
        assertEquals(0L, resposta.total());
        assertTrue(resposta.atendimentos().isEmpty());
        verifyNoInteractions(complementos, prescricoes, exames);
    }
}
