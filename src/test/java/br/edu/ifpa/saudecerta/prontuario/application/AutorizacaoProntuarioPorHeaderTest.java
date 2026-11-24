package br.edu.ifpa.saudecerta.prontuario.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import br.edu.ifpa.saudecerta.agenda.AgendaApi;
import br.edu.ifpa.saudecerta.agenda.ConsultaResumo;
import br.edu.ifpa.saudecerta.prontuario.domain.Atendimento;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AutorizacaoProntuarioPorHeaderTest {

    @AfterEach
    void limparContexto() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void headerAusenteResultaEmUsuarioNulo() {
        prepararRequisicao(null);
        var autorizacao = criarAdapter(mock(AtendimentoRepository.class), mock(ObjectProvider.class));
        assertNull(autorizacao.exigirUsuarioAutorizadoParaRegistrarAtendimento(10L));
        assertNull(autorizacao.exigirUsuarioAutorizadoParaConsultarHistorico(20L));
        assertNull(autorizacao.exigirUsuarioAutorizadoParaAlterarExame(30L));
    }

    @Test
    void headerPresenteEValidoForneceUsuario() {
        prepararRequisicao("15");
        var autorizacao = criarAdapter(mock(AtendimentoRepository.class), mock(ObjectProvider.class));
        assertEquals(Long.valueOf(15L), autorizacao.exigirUsuarioAutorizadoParaRegistrarAtendimento(10L));
    }

    @Test
    void headerInvalidoERejeitado() {
        prepararRequisicao("invalido");
        var autorizacao = criarAdapter(mock(AtendimentoRepository.class), mock(ObjectProvider.class));
        assertThrows(RegraDeNegocioException.class,
                () -> autorizacao.exigirUsuarioAutorizadoParaRegistrarAtendimento(10L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void complementoUsaProfissionalDaAgendaVinculada() {
        prepararRequisicao("15");
        AtendimentoRepository repositorio = mock(AtendimentoRepository.class);
        ObjectProvider<AgendaApi> provider = mock(ObjectProvider.class);
        AgendaApi agenda = mock(AgendaApi.class);
        when(provider.getIfAvailable()).thenReturn(agenda);
        when(repositorio.buscarPorId(40L)).thenReturn(Optional.of(
                new Atendimento(40L, 20L, 30L, "Queixa", "Evolucao", "Conduta", Instant.now())));
        when(agenda.buscarConsulta(30L)).thenReturn(Optional.of(
                new ConsultaResumo(30L, 50L, 60L, LocalDateTime.now(), "AGENDADA")));
        var autorizacao = criarAdapter(repositorio, provider);
        var resultado = autorizacao.exigirProfissionalAutorizadoParaComplementarAtendimento(40L);
        assertEquals(Long.valueOf(60L), resultado.profissionalId());
        assertEquals(Long.valueOf(15L), resultado.usuarioId());
    }

    private AutorizacaoProntuarioPorHeader criarAdapter(
            AtendimentoRepository repositorio, ObjectProvider<AgendaApi> agenda) {
        return new AutorizacaoProntuarioPorHeader(repositorio, agenda);
    }

    private void prepararRequisicao(String usuarioId) {
        var requisicao = new MockHttpServletRequest();
        if (usuarioId != null) {
            requisicao.addHeader("X-Usuario-Id", usuarioId);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requisicao));
    }
}