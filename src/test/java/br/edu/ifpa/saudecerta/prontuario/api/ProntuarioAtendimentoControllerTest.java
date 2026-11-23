package br.edu.ifpa.saudecerta.prontuario.api;

import br.edu.ifpa.saudecerta.prontuario.application.AutorizacaoProntuario;
import br.edu.ifpa.saudecerta.prontuario.application.RegistrarAtendimentoFacade;
import br.edu.ifpa.saudecerta.shared.excecoes.GlobalExceptionHandler;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProntuarioAtendimentoControllerTest {
    private static final String URL = "/api/consultas/800/atendimento";

    @Test
    void registraComIdentidadeAutorizada() throws Exception {
        var service = mock(RegistrarAtendimentoFacade.class);
        var autorizacao = mock(AutorizacaoProntuario.class);
        when(autorizacao.exigirUsuarioAutorizadoParaRegistrarAtendimento(800L))
                .thenReturn(9L);
        when(service.registrar(800L, 9L, "Dor abdominal",
                "Paciente consciente", "Solicitar exames")).thenReturn(300L);

        var mvc = MockMvcBuilders.standaloneSetup(
                new ProntuarioAtendimentoController(service, autorizacao)).build();

        mvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"queixa\":\"Dor abdominal\","
                        + "\"evolucao\":\"Paciente consciente\","
                        + "\"conduta\":\"Solicitar exames\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(300));

        verify(service).registrar(800L, 9L, "Dor abdominal",
                "Paciente consciente", "Solicitar exames");
    }

    @Test
    void rejeitaCamposVaziosAntesDaAutorizacao() throws Exception {
        var service = mock(RegistrarAtendimentoFacade.class);
        var autorizacao = mock(AutorizacaoProntuario.class);
        var mvc = MockMvcBuilders.standaloneSetup(
                new ProntuarioAtendimentoController(service, autorizacao))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"queixa\":\"\", \"evolucao\":\"\", \"conduta\":\"\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service, autorizacao);
    }

    @Test
    void retorna404QuandoConsultaNaoExiste() throws Exception {
        var service = mock(RegistrarAtendimentoFacade.class);
        var autorizacao = mock(AutorizacaoProntuario.class);
        when(autorizacao.exigirUsuarioAutorizadoParaRegistrarAtendimento(800L))
                .thenReturn(9L);
        when(service.registrar(800L, 9L, "Dor",
                "Consciente", "Exames")).thenThrow(
                    new RecursoNaoEncontradoException("Consulta nao encontrada."));

        var mvc = MockMvcBuilders.standaloneSetup(
                new ProntuarioAtendimentoController(service, autorizacao))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"queixa\":\"Dor\", \"evolucao\":\"Consciente\", \"conduta\":\"Exames\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void proibePutEDelete() throws Exception {
        var service = mock(RegistrarAtendimentoFacade.class);
        var autorizacao = mock(AutorizacaoProntuario.class);
        var mvc = MockMvcBuilders.standaloneSetup(
                new ProntuarioAtendimentoController(service, autorizacao))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(put(URL)).andExpect(status().isMethodNotAllowed());
        mvc.perform(delete(URL)).andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(service, autorizacao);
    }
}
