package br.edu.ifpa.saudecerta.seguranca.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import br.edu.ifpa.saudecerta.seguranca.auditoria.domain.AuditoriaRepository;
import br.edu.ifpa.saudecerta.shared.eventos.AcessoNegado;
import br.edu.ifpa.saudecerta.shared.eventos.AtendimentoRegistrado;
import br.edu.ifpa.saudecerta.shared.eventos.ProntuarioAcessado;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** ADR-002 com falhas: o que acontece quando a gravação da auditoria dá erro em cada um dos três caminhos. */
@SpringBootTest
class AuditoriaFalhaTest {

    @Autowired ApplicationEventPublisher eventos;
    @Autowired TransactionTemplate transacao;
    @Autowired JdbcTemplate jdbc;
    @SpyBean AuditoriaRepository repositorio;

    @BeforeEach
    void prepara() {
        jdbc.update("delete from aud_registro_auditoria");
        jdbc.execute("create table if not exists teste_alteracao_clinica (id int)");
        jdbc.update("delete from teste_alteracao_clinica");
    }

    private int auditados() {
        return jdbc.queryForObject("select count(*) from aud_registro_auditoria", Integer.class);
    }

    @Test
    void falhaNaAuditoriaDeEscritaDesfazAAlteracaoClinica() {
        doThrow(new IllegalStateException("falha simulada")).when(repositorio).salvar(any());

        assertThatThrownBy(() -> transacao.executeWithoutResult(status -> {
            jdbc.update("insert into teste_alteracao_clinica values (1)"); // a "alteração clínica"
            eventos.publishEvent(new AtendimentoRegistrado(10L, 5L, 7L, "ATENDIMENTO", Instant.now()));
        })).hasRootCauseMessage("falha simulada");

        assertThat(jdbc.queryForObject("select count(*) from teste_alteracao_clinica", Integer.class)).isZero();
        assertThat(auditados()).isZero();
    }

    @Test
    void falhaNaAuditoriaDeAcessoNegadoNaoPropagaEOAcessoContinuaNegado() {
        doThrow(new IllegalStateException("falha simulada")).when(repositorio).salvar(any());

        eventos.publishEvent(new AcessoNegado(9L, "GET /api/auditoria", "perfil", Instant.now()));
        // chegar aqui sem exceção: quem negou o acesso segue devolvendo 403

        assertThat(auditados()).isZero();
    }

    @Test
    void leituraTentaTresVezesEGravaSeAUltimaTentativaDerCerto() {
        doThrow(new IllegalStateException("1")).doThrow(new IllegalStateException("2"))
                .doCallRealMethod().when(repositorio).salvar(any());

        eventos.publishEvent(new ProntuarioAcessado(5L, 7L, Instant.now()));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(auditados()).isEqualTo(1));
        verify(repositorio, times(3)).salvar(any());
    }

    @Test
    void leituraDesisteDepoisDeTresTentativasSemQuebrarQuemLeu() {
        doThrow(new IllegalStateException("falha simulada")).when(repositorio).salvar(any());

        eventos.publishEvent(new ProntuarioAcessado(5L, 7L, Instant.now()));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(repositorio, times(3)).salvar(any()));
        assertThat(auditados()).isZero();
    }
}
