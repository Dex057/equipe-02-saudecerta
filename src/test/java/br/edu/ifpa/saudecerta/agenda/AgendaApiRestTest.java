package br.edu.ifpa.saudecerta.agenda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ifpa.saudecerta.agenda.domain.Consulta;
import br.edu.ifpa.saudecerta.agenda.domain.ConsultaRepository;
import br.edu.ifpa.saudecerta.agenda.domain.ConflitoDeHorarioException;
import br.edu.ifpa.saudecerta.agenda.application.AgendaService;
import br.edu.ifpa.saudecerta.cadastro.CadastroApi;
import br.edu.ifpa.saudecerta.cadastro.PacienteResumo;
import br.edu.ifpa.saudecerta.cadastro.ProfissionalResumo;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AgendaApiRestTest.ClockConfig.class)
class AgendaApiRestTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 1, 5);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AgendaService agenda;

    @Autowired
    private ConsultaRepository consultas;

    @MockBean
    private CadastroApi cadastro;

    @BeforeEach
    void limparEPrepararCadastros() {
        jdbc.update("DELETE FROM age_consulta");
        when(cadastro.buscarPaciente(1L)).thenReturn(Optional.of(new PacienteResumo(1L, "Paciente", true)));
        when(cadastro.buscarPaciente(2L)).thenReturn(Optional.of(new PacienteResumo(2L, "Inativo", false)));
        when(cadastro.buscarProfissional(10L))
                .thenReturn(Optional.of(new ProfissionalResumo(10L, "Profissional", 1L, true)));
        when(cadastro.buscarProfissional(11L))
                .thenReturn(Optional.of(new ProfissionalResumo(11L, "Inativo", 1L, false)));
    }

    @Test
    void marcaConsultaValidaERejeitaCadastroInativoOuHorarioForaDaGrade() throws Exception {
        mockMvc.perform(post("/api/consultas").contentType("application/json")
                        .content(corpo(1, 10, HOJE.atTime(8, 0))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/consultas").contentType("application/json")
                        .content(corpo(1, 10, HOJE.atTime(8, 15))))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post("/api/consultas").contentType("application/json")
                        .content(corpo(2, 10, HOJE.atTime(8, 30))))
                .andExpect(status().isUnprocessableEntity());

        when(cadastro.buscarProfissional(12L)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/consultas").contentType("application/json")
                        .content(corpo(1, 12, HOJE.atTime(8, 30))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void remarcacaoConflitanteFazRollbackECancelamentoLiberaHorario() throws Exception {
        Long primeira = criar(HOJE.atTime(8, 0));
        Long segunda = criar(HOJE.atTime(8, 30));

        mockMvc.perform(patch("/api/consultas/" + primeira).contentType("application/json")
                        .content("{\"dataHora\":\"2026-01-05T08:30:00\"}"))
                .andExpect(status().isConflict());
        assertEquals(HOJE.atTime(8, 0), consultas.buscar(primeira).orElseThrow().getDataHora());

        mockMvc.perform(patch("/api/consultas/" + segunda).contentType("application/json")
                        .content("{\"status\":\"CANCELADA\"}"))
                .andExpect(status().isOk());
        criar(HOJE.atTime(8, 30));
    }

    @Test
    void conflitoNomeadoDaRestricaoUnicaETraduzidoSemExporExcecaoDoBanco() {
        consultas.salvar(Consulta.agendada(1L, 10L, HOJE.atTime(9, 0)));

        assertThrows(ConflitoDeHorarioException.class,
                () -> consultas.salvar(Consulta.agendada(1L, 10L, HOJE.atTime(9, 0))));
    }

    @Test
    void duasMarcacoesConcorrentesDoMesmoSlotResultamEmUmaCriacaoEUmConflito() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> primeira = executor.submit(() -> postarConcorrentemente(inicio));
            Future<Integer> segunda = executor.submit(() -> postarConcorrentemente(inicio));
            inicio.countDown();
            int statusA = primeira.get();
            int statusB = segunda.get();
            assertEquals(201, (statusA == 201 ? statusA : statusB));
            assertEquals(409, (statusA == 409 ? statusA : statusB));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void horarioFuturoNaoPodeSerMarcadoRealizadoECancelamentoExigeAgendada() throws Exception {
        Long consultaId = criar(LocalDate.of(2026, 1, 6).atTime(8, 0));
        assertThrows(RegraDeNegocioException.class, () -> agenda.marcarRealizada(consultaId));

        Long canceladaId = criar(HOJE.atTime(9, 0));
        mockMvc.perform(patch("/api/consultas/" + canceladaId).contentType("application/json")
                        .content("{\"status\":\"CANCELADA\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/consultas/" + canceladaId).contentType("application/json")
                        .content("{\"status\":\"CANCELADA\"}"))
                .andExpect(status().isUnprocessableEntity());

        Long realizadaId = criar(HOJE.atTime(9, 30));
        agenda.marcarRealizada(realizadaId);
        mockMvc.perform(patch("/api/consultas/" + realizadaId).contentType("application/json")
                        .content("{\"status\":\"CANCELADA\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void apiPublicaBuscaConsultaEApenasConsultaAgendadaOcupaHorario() {
        Long consultaId = criar(HOJE.atTime(10, 0));
        assertFalse(agenda.buscarConsulta(consultaId).isEmpty());
        agenda.marcarRealizada(consultaId);
        criar(HOJE.atTime(10, 0));
    }

    @Test
    void consultaPodeSerBuscadaPorIdEAusenteRetorna404() throws Exception {
        Long consultaId = criar(HOJE.atTime(10, 30));

        mockMvc.perform(get("/api/consultas/" + consultaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(consultaId));
        mockMvc.perform(get("/api/consultas/999999"))
                .andExpect(status().isNotFound());
    }

    private Long criar(LocalDateTime dataHora) {
        return agenda.marcar(1L, 10L, dataHora).id();
    }

    private int postarConcorrentemente(CountDownLatch inicio) throws Exception {
        inicio.await();
        MvcResult resultado = mockMvc.perform(post("/api/consultas").contentType("application/json")
                        .content(corpo(1, 10, HOJE.atTime(11, 0))))
                .andReturn();
        return resultado.getResponse().getStatus();
    }

    private static String corpo(long pacienteId, long profissionalId, LocalDateTime dataHora) {
        return "{\"pacienteId\":" + pacienteId + ",\"profissionalId\":" + profissionalId
                + ",\"dataHora\":\"" + dataHora + "\"}";
    }

    @TestConfiguration
    static class ClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-01-05T12:00:00Z"), ZoneId.of("America/Belem"));
        }
    }
}
