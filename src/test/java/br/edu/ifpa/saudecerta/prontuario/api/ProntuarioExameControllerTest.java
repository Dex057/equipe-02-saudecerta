
package br.edu.ifpa.saudecerta.prontuario.api;

import br.edu.ifpa.saudecerta.prontuario.application.AlterarStatusExameService;
import br.edu.ifpa.saudecerta.prontuario.application.AutorizacaoProntuario;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProntuarioExameControllerTest {

    @Test
    void deveAlterarStatusComUsuarioAutorizado()
            throws Exception {

        var service = mock(AlterarStatusExameService.class);
        var autorizacao = mock(AutorizacaoProntuario.class);

        when(autorizacao
            .exigirUsuarioAutorizadoParaAlterarExame(55L))
            .thenReturn(9L);

        MockMvc mvc = MockMvcBuilders
            .standaloneSetup(
                new ProntuarioExameController(
                    service,
                    autorizacao
                )
            )
            .build();

        mvc.perform(
            patch("/api/exames/55/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"REALIZADO\"}")
        )
        .andExpect(status().isNoContent());

        verify(autorizacao)
            .exigirUsuarioAutorizadoParaAlterarExame(55L);

        verify(service)
            .alterarPorTexto(55L, 9L, "REALIZADO");
    }
}
