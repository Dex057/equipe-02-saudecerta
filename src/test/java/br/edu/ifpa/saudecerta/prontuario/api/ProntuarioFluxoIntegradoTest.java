package br.edu.ifpa.saudecerta.prontuario.api;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.edu.ifpa.saudecerta.agenda.AgendaApi;
import br.edu.ifpa.saudecerta.agenda.ConsultaResumo;
import br.edu.ifpa.saudecerta.prontuario.application.AutorizacaoProntuario;
import br.edu.ifpa.saudecerta.prontuario.domain.SolicitacaoDeExame;
import br.edu.ifpa.saudecerta.prontuario.domain.SolicitacaoDeExameRepository;
import br.edu.ifpa.saudecerta.prontuario.infra.PrescricaoEntity;
import br.edu.ifpa.saudecerta.prontuario.infra.PrescricaoJpaRepository;
import br.edu.ifpa.saudecerta.prontuario.infra.ItemDePrescricaoEntity;
import br.edu.ifpa.saudecerta.prontuario.infra.ItemDePrescricaoJpaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste integrado com H2/Flyway/JPA/HTTP.
 * Agenda e autenticacao sao mockadas somente durante o teste.
 * Em producao a flag da API permanece desabilitada ate a integracao real.
 */
@SpringBootTest(properties = "saudecerta.prontuario.api-habilitada=true")
@AutoConfigureMockMvc
@Transactional
class ProntuarioFluxoIntegradoTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired PrescricaoJpaRepository prescricoes;
    @Autowired ItemDePrescricaoJpaRepository itens;
    @Autowired SolicitacaoDeExameRepository exames;

    @MockBean AgendaApi agendaApi;
    @MockBean AutorizacaoProntuario autorizacao;

    @Test
    void registraConsultaEConsultaHistoricoCompletoSemEditarAtendimento() throws Exception {
        when(agendaApi.buscarConsulta(800L))
            .thenReturn(Optional.of(new ConsultaResumo(
                800L, 300L, 7L, LocalDateTime.of(2026, 11, 21, 9, 0), "AGENDADA")));
        when(autorizacao.exigirUsuarioAutorizadoParaRegistrarAtendimento(800L))
            .thenReturn(9L);
        when(autorizacao.exigirUsuarioAutorizadoParaConsultarHistorico(300L))
            .thenReturn(9L);

        String registro = mvc.perform(post("/api/consultas/800/atendimento")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"queixa\":\"Dor abdominal\","
                    + "\"evolucao\":\"Paciente consciente\","
                    + "\"conduta\":\"Solicitar exames\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long atendimentoId = mapper.readTree(registro).get("id").asLong();
        assertTrue(atendimentoId > 0);
        verify(agendaApi).marcarRealizada(800L);

        when(autorizacao.exigirProfissionalAutorizadoParaComplementarAtendimento(atendimentoId))
            .thenReturn(new AutorizacaoProntuario.ProfissionalAutorizado(7L, 9L));

        mvc.perform(post("/api/atendimentos/" + atendimentoId + "/complementos")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"texto\":\"Paciente apresentou melhora\"}"))
            .andExpect(status().isCreated());

        var prescricao = prescricoes.inserir(
            new PrescricaoEntity(atendimentoId, Instant.now()));
        itens.inserir(new ItemDePrescricaoEntity(
            prescricao.getId(), "Dipirona", "500mg", "8/8h", "3 dias"));

        var exame = exames.inserir(new SolicitacaoDeExame(null, atendimentoId));
        when(autorizacao.exigirUsuarioAutorizadoParaAlterarExame(exame.getId()))
            .thenReturn(9L);
        mvc.perform(patch("/api/exames/" + exame.getId() + "/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"REALIZADO\"}"))
            .andExpect(status().isNoContent());

        var historico = mvc.perform(get("/api/pacientes/300/prontuario?page=0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.atendimentos[0].queixa").value("Dor abdominal"))
            .andExpect(jsonPath("$.atendimentos[0].complementos[0].texto")
                .value("Paciente apresentou melhora"))
            .andExpect(jsonPath("$.atendimentos[0].prescricoes[0].itens[0].medicamento")
                .value("Dipirona"))
            .andExpect(jsonPath("$.atendimentos[0].exames[0].status")
                .value("REALIZADO"))
            .andReturn().getResponse().getContentAsString();

        // RN05: complemento cria um novo registro, nao altera os campos originais.
        assertEquals("Solicitar exames", mapper.readTree(historico)
            .path("atendimentos").get(0).path("conduta").asText());

        // A segunda transicao do mesmo exame deve ser rejeitada.
        mvc.perform(patch("/api/exames/" + exame.getId() + "/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"CANCELADO\"}"))
            .andExpect(status().isUnprocessableEntity());
    }
}
