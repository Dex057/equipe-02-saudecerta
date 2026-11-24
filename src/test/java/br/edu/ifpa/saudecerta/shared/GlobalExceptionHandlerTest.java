package br.edu.ifpa.saudecerta.shared;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ifpa.saudecerta.shared.excecoes.ConflitoException;
import br.edu.ifpa.saudecerta.shared.excecoes.GlobalExceptionHandler;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Garante o mapeamento exceção → status HTTP → envelope {codigo, mensagem, timestamp} do contrato REST. */
class GlobalExceptionHandlerTest {

    @RestController
    static class ControllerDeTeste {

        record Entrada(@NotBlank String nome) {
        }

        @GetMapping("/t/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("Paciente 7 não existe.");
        }

        @GetMapping("/t/conflito")
        void conflito() {
            throw new ConflitoException("Horário já ocupado.");
        }

        @GetMapping("/t/regra")
        void regra() {
            throw new RegraDeNegocioException("Consulta futura não pode ser realizada.");
        }

        @GetMapping("/t/erro")
        void erro() {
            throw new IllegalStateException("detalhe interno que não pode vazar");
        }

        @GetMapping("/t/restricao")
        void restricao() {
            throw new ConstraintViolationException(Set.of());
        }

        @GetMapping("/t/parametro")
        void parametro(@RequestParam int quantidade) {
        }

        @PostMapping("/t/validacao")
        void validacao(@Valid @RequestBody Entrada entrada) {
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void monta() {
        mvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void naoEncontradoVira404() throws Exception {
        mvc.perform(get("/t/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void conflitoVira409() throws Exception {
        mvc.perform(get("/t/conflito"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"));
    }

    @Test
    void regraDeNegocioVira422() throws Exception {
        mvc.perform(get("/t/regra"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"));
    }

    @Test
    void validacaoVira400ComOCampoNaMensagem() throws Exception {
        mvc.perform(post("/t/validacao").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("nome")));
    }

    @Test
    void jsonMalformadoEParametroInvalidoViram400() throws Exception {
        mvc.perform(post("/t/validacao").contentType(MediaType.APPLICATION_JSON).content("{nao-e-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
        mvc.perform(get("/t/parametro").param("quantidade", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
    }

    @Test
    void violacaoDeRestricaoDeParametroVira400() throws Exception {
        mvc.perform(get("/t/restricao"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void metodoNaoPermitidoVira405() throws Exception {
        mvc.perform(post("/t/conflito"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Allow", "GET"))
                .andExpect(jsonPath("$.codigo").value("METODO_NAO_PERMITIDO"));
    }

    @Test
    void tipoDeConteudoNaoSuportadoVira415() throws Exception {
        mvc.perform(post("/t/validacao").contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value("TIPO_NAO_SUPORTADO"));
    }

    @Test
    void erroInesperadoVira500SemVazarDetalhe() throws Exception {
        mvc.perform(get("/t/erro"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"))
                .andExpect(jsonPath("$.mensagem").value("Erro interno."));
    }
}
