
package br.edu.ifpa.saudecerta.prontuario.api;

import br.edu.ifpa.saudecerta.prontuario.application.AutorizacaoProntuario;
import br.edu.ifpa.saudecerta.prontuario.application.ComplementarAtendimentoService;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProntuarioComplementoControllerTest {

    @Test
    void deveCriarComplementoComProfissionalAutorizado()
            throws Exception {

        var service = mock(ComplementarAtendimentoService.class);
        var autorizacao = mock(AutorizacaoProntuario.class);

        when(autorizacao
            .exigirProfissionalAutorizadoParaComplementarAtendimento(55L))
            .thenReturn(
                new AutorizacaoProntuario.ProfissionalAutorizado(
                    7L,
                    9L
                )
            );

        when(service.complementarRetornandoId(
            55L, 7L, 9L, "Nova observacao"
        )).thenReturn(101L);

        MockMvc mvc = MockMvcBuilders
            .standaloneSetup(
                new ProntuarioComplementoController(
                    service,
                    autorizacao
                )
            )
            .build();

        mvc.perform(
            post("/api/atendimentos/55/complementos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"texto\":\"Nova observacao\"}")
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(101));

        verify(service).complementarRetornandoId(
            55L, 7L, 9L, "Nova observacao"
        );
    }
}
