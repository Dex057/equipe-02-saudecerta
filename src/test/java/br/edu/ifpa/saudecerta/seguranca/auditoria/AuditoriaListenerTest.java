package br.edu.ifpa.saudecerta.seguranca.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import br.edu.ifpa.saudecerta.shared.eventos.AcessoNegado;
import br.edu.ifpa.saudecerta.shared.eventos.AtendimentoRegistrado;
import br.edu.ifpa.saudecerta.shared.eventos.ProntuarioAcessado;
import br.edu.ifpa.saudecerta.shared.eventos.StatusExameAlterado;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** ADR-002 sem falhas: cada evento vira exatamente um registro, e a escrita entra na transação de quem publicou. */
@SpringBootTest
class AuditoriaListenerTest {

    @Autowired ApplicationEventPublisher eventos;
    @Autowired TransactionTemplate transacao;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void limpa() {
        jdbc.update("delete from aud_registro_auditoria");
    }

    private int total() {
        return jdbc.queryForObject("select count(*) from aud_registro_auditoria", Integer.class);
    }

    @Test
    void atendimentoRegistradoGeraUmRegistroDeEscritaNaMesmaTransacao() {
        Instant agora = Instant.parse("2026-11-18T12:00:00Z");

        transacao.executeWithoutResult(status -> {
            eventos.publishEvent(new AtendimentoRegistrado(10L, 5L, 7L, "ATENDIMENTO", agora));
            // ainda dentro da transação de quem publicou, o registro já existe (listener síncrono)
            assertThat(total()).isEqualTo(1);
            status.setRollbackOnly();
        });

        // desfeita a transação, o registro some junto: ele estava nela
        assertThat(total()).isZero();

        eventos.publishEvent(new AtendimentoRegistrado(10L, 5L, 7L, "ATENDIMENTO", agora));
        List<Map<String, Object>> linhas = jdbc.queryForList("select * from aud_registro_auditoria");
        assertThat(linhas).hasSize(1);
        assertThat(linhas.get(0)).containsEntry("tipo", "ESCRITA").containsEntry("paciente_id", 5L)
                .containsEntry("usuario_id", 7L).containsEntry("acao", "ATENDIMENTO_REGISTRADO");
    }

    @Test
    void statusDeExameAlteradoGeraUmRegistroComStatusAnteriorENovo() {
        eventos.publishEvent(new StatusExameAlterado(3L, 10L, 7L, "SOLICITADO", "REALIZADO",
                Instant.parse("2026-11-18T12:00:00Z")));

        assertThat(total()).isEqualTo(1);
        Map<String, Object> linha = jdbc.queryForMap("select * from aud_registro_auditoria");
        assertThat(linha).containsEntry("tipo", "ESCRITA").containsEntry("acao", "STATUS_EXAME_ALTERADO");
        assertThat((String) linha.get("detalhe")).contains("SOLICITADO -> REALIZADO")
                .contains("solicitacaoExameId=3").contains("atendimentoId=10");
        assertThat(linha.get("data_hora")).isNotNull();
    }

    @Test
    void acessoNegadoEGravadoDeFormaSincrona() {
        eventos.publishEvent(new AcessoNegado(9L, "GET /api/pacientes/5/prontuario", "perfil RECEPCAO",
                Instant.now()));

        assertThat(total()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select tipo from aud_registro_auditoria", String.class))
                .isEqualTo("ACESSO_NEGADO");
    }

    @Test
    void leituraDoProntuarioEGravadaDeFormaAssincrona() {
        eventos.publishEvent(new ProntuarioAcessado(5L, 7L, Instant.now()));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(total()).isEqualTo(1));
        assertThat(jdbc.queryForObject("select tipo from aud_registro_auditoria", String.class))
                .isEqualTo("LEITURA");
    }
}
