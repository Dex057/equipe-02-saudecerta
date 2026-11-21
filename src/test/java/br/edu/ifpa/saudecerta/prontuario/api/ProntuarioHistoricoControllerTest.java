
package br.edu.ifpa.saudecerta.prontuario.api;

import java.util.List;

import br.edu.ifpa.saudecerta.prontuario.application.AutorizacaoProntuario;
import br.edu.ifpa.saudecerta.prontuario.application.ConsultarHistoricoService;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProntuarioHistoricoControllerTest {

    @Test
    void deveConsultarHistoricoComAutorizacao()
            throws Exception {

        var service = mock(ConsultarHistoricoService.class);
        var autorizacao = mock(AutorizacaoProntuario.class);

        when(autorizacao
            .exigirUsuarioAutorizadoParaConsultarHistorico(300L))
            .thenReturn(9L);

        when(service.consultarResumo(300L, 9L, 0))
            .thenReturn(
                new ConsultarHistoricoService.HistoricoResumo(
                    300L,
                    0,
                    10,
                    0L,
                    List.of()
                )
            );

        var mvc = MockMvcBuilders
            .standaloneSetup(
                new ProntuarioHistoricoController(
                    service,
                    autorizacao
                )
            )
            .build();

        mvc.perform(
            get("/api/pacientes/300/prontuario")
                .param("page", "0")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pacienteId").value(300))
        .andExpect(jsonPath("$.total").value(0));

        verify(autorizacao)
            .exigirUsuarioAutorizadoParaConsultarHistorico(300L);

        verify(service).consultarResumo(300L, 9L, 0);
    }
}
