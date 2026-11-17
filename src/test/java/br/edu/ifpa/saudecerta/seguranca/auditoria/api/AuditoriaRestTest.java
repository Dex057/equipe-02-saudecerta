package br.edu.ifpa.saudecerta.seguranca.auditoria.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ifpa.saudecerta.shared.eventos.AtendimentoRegistrado;
import br.edu.ifpa.saudecerta.shared.eventos.StatusExameAlterado;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuditoriaRestTest {

    @Autowired MockMvc mvc;
    @Autowired ApplicationEventPublisher eventos;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void limpa() {
        jdbc.update("delete from aud_registro_auditoria");
    }

    @Test
    void consultaFiltraPorPacienteUsuarioEPeriodoComOMaisRecentePrimeiro() throws Exception {
        eventos.publishEvent(new AtendimentoRegistrado(1L, 5L, 7L, "ATENDIMENTO",
                Instant.parse("2026-11-10T10:00:00Z")));
        eventos.publishEvent(new AtendimentoRegistrado(2L, 5L, 8L, "PRESCRICAO",
                Instant.parse("2026-11-12T10:00:00Z")));
        eventos.publishEvent(new AtendimentoRegistrado(3L, 6L, 7L, "ATENDIMENTO",
                Instant.parse("2026-11-14T10:00:00Z")));
        eventos.publishEvent(new StatusExameAlterado(9L, 3L, 7L, "SOLICITADO", "CANCELADO",
                Instant.parse("2026-11-15T10:00:00Z")));

        mvc.perform(get("/api/auditoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.conteudo[0].acao").value("STATUS_EXAME_ALTERADO"));
        mvc.perform(get("/api/auditoria").param("pacienteId", "5"))
                .andExpect(jsonPath("$.conteudo", hasSize(2)))
                .andExpect(jsonPath("$.conteudo[0].detalhe").value("atendimentoId=2; tipo=PRESCRICAO"));
        mvc.perform(get("/api/auditoria").param("usuarioId", "7"))
                .andExpect(jsonPath("$.totalElementos").value(3));
        mvc.perform(get("/api/auditoria").param("de", "2026-11-11T00:00:00Z").param("ate", "2026-11-14T23:59:59Z"))
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(get("/api/auditoria").param("size", "1").param("page", "1"))
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.totalPaginas").value(4));
    }

    @Test
    void parametroInvalidoDa400() throws Exception {
        mvc.perform(get("/api/auditoria").param("de", "ontem"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
    }
}
