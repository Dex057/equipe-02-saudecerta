package br.edu.ifpa.saudecerta.cadastro.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Ponta a ponta do Cadastro: HTTP, validação, serviço, JPA e banco. */
@SpringBootTest
@AutoConfigureMockMvc
class CadastroApiRestTest {

    private static final String CPF_ANA = "529.982.247-25";
    private static final String CPF_JOSE = "111.444.777-35";
    private static final String CPF_JOANA = "935.411.347-80";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void limpa() {
        jdbc.update("delete from cad_profissional_especialidade");
        jdbc.update("delete from cad_profissional");
        jdbc.update("delete from cad_especialidade");
        jdbc.update("delete from cad_paciente");
    }

    private ResultActions postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions putJson(String url, String json) throws Exception {
        return mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String paciente(String nome, String cpf, Boolean ativo) {
        String campoAtivo = ativo == null ? "" : ", \"ativo\": " + ativo;
        return "{\"nome\": \"" + nome + "\", \"cpf\": \"" + cpf + "\", \"dataNascimento\": \"1990-04-15\","
                + " \"telefone\": \"91 99999-0000\", \"email\": \"a@example.test\"" + campoAtivo + "}";
    }

    private long criaPaciente(String nome, String cpf) throws Exception {
        String corpo = postJson("/api/pacientes", paciente(nome, cpf, null)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(corpo.replaceAll("^\\{\"id\":(\\d+).*", "$1"));
    }

    // ---- RF01: pacientes

    @Test
    void cadastraPacienteEDevolve201ComLocationECpfSoComDigitos() throws Exception {
        postJson("/api/pacientes", paciente("Ana Souza", CPF_ANA, null))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/pacientes/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void cpfDuplicadoEntreAtivosDa409() throws Exception {
        criaPaciente("Ana Souza", CPF_ANA);
        postJson("/api/pacientes", paciente("Outra Ana", "52998224725", null))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"));
    }

    @Test
    void entradaInvalidaDa400ComOCampoNaMensagem() throws Exception {
        postJson("/api/pacientes", paciente("", CPF_ANA, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.mensagem").value(containsString("nome")));
        postJson("/api/pacientes", paciente("Ana", "123", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("cpf")));
        postJson("/api/pacientes",
                "{\"nome\":\"Ana\",\"cpf\":\"" + CPF_ANA + "\",\"dataNascimento\":\"2999-01-01\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("dataNascimento")));
        postJson("/api/pacientes",
                "{\"nome\":\"Ana\",\"cpf\":\"" + CPF_ANA + "\",\"dataNascimento\":\"1990-01-01\","
                        + "\"email\":\"nao-e-email\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("email")));
    }

    @Test
    void cpfComDigitosVerificadoresErradosDa422() throws Exception {
        postJson("/api/pacientes", paciente("Ana", "529.982.247-24", null))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"));
    }

    @Test
    void pacienteInexistenteDa404() throws Exception {
        mvc.perform(get("/api/pacientes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
    }

    @Test
    void buscaPorPrefixoDoNomeSemDiferenciarMaiusculasNemAcentoEPagina() throws Exception {
        criaPaciente("José da Silva", CPF_JOSE);
        criaPaciente("Joana Lima", CPF_JOANA);
        criaPaciente("Ana Souza", CPF_ANA);

        mvc.perform(get("/api/pacientes").param("nome", "jose"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].nome").value("José da Silva"));
        mvc.perform(get("/api/pacientes").param("nome", "JO"))
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(get("/api/pacientes").param("nome", "jo").param("size", "1"))
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        mvc.perform(get("/api/pacientes"))
                .andExpect(jsonPath("$.totalElementos").value(3));
    }

    @Test
    void atualizaDadosDoPaciente() throws Exception {
        long id = criaPaciente("Ana Souza", CPF_ANA);
        putJson("/api/pacientes/" + id, paciente("Ana Souza Lima", CPF_ANA, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza Lima"));
        mvc.perform(get("/api/pacientes/" + id)).andExpect(jsonPath("$.nome").value("Ana Souza Lima"));
    }

    @Test
    void inativarLiberaOCpfEReativarReaplicaARn01() throws Exception {
        long primeiro = criaPaciente("Ana Souza", CPF_ANA);
        putJson("/api/pacientes/" + primeiro, paciente("Ana Souza", CPF_ANA, false))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));

        // com o primeiro inativo, o CPF pode ser cadastrado de novo
        postJson("/api/pacientes", paciente("Ana Nova", CPF_ANA, null)).andExpect(status().isCreated());

        // reativar o primeiro agora colide com o ativo (ADR-006)
        putJson("/api/pacientes/" + primeiro, paciente("Ana Souza", CPF_ANA, true))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/pacientes/" + primeiro)).andExpect(jsonPath("$.ativo").value(false));
    }

    // ---- RF02: especialidades e profissionais

    private long criaEspecialidade(String nome) throws Exception {
        String corpo = postJson("/api/especialidades", "{\"nome\":\"" + nome + "\"}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return Long.parseLong(corpo.replaceAll("^\\{\"id\":(\\d+).*", "$1"));
    }

    @Test
    void especialidadeRepetidaDa409() throws Exception {
        criaEspecialidade("Cardiologia");
        postJson("/api/especialidades", "{\"nome\":\"Cardiologia\"}").andExpect(status().isConflict());
        mvc.perform(get("/api/especialidades")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void profissionalExigeEspecialidadeEElaTemDeExistir() throws Exception {
        postJson("/api/profissionais", "{\"nome\":\"Dr. Caio\",\"especialidadeIds\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("especialidadeIds")));
        postJson("/api/profissionais", "{\"nome\":\"Dr. Caio\",\"especialidadeIds\":[9999]}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"));
    }

    @Test
    void cadastraProfissionalListaPorEspecialidadeEAtualiza() throws Exception {
        long cardio = criaEspecialidade("Cardiologia");
        long pediatria = criaEspecialidade("Pediatria");

        String corpo = postJson("/api/profissionais",
                "{\"nome\":\"Dr. Caio\",\"especialidadeIds\":[" + cardio + "]}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.especialidades[0].nome").value("Cardiologia"))
                .andReturn().getResponse().getContentAsString();
        long profissionalId = Long.parseLong(corpo.replaceAll("^\\{\"id\":(\\d+).*", "$1"));
        postJson("/api/profissionais", "{\"nome\":\"Dra. Lia\",\"especialidadeIds\":[" + pediatria + "]}")
                .andExpect(status().isCreated());

        mvc.perform(get("/api/profissionais")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/profissionais").param("especialidadeId", String.valueOf(cardio)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Dr. Caio"));

        putJson("/api/profissionais/" + profissionalId,
                "{\"nome\":\"Dr. Caio\",\"especialidadeIds\":[" + cardio + "," + pediatria + "],"
                        + "\"ativo\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidades", hasSize(2)))
                .andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(get("/api/profissionais").param("especialidadeId", String.valueOf(pediatria)))
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
