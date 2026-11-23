
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.time.Instant;

import br.edu.ifpa.saudecerta.prontuario.domain.StatusExame;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AtendimentoJpaRepository.class)
class SolicitacaoDeExameJpaRepositoryTest {

    @Autowired
    private ProntuarioJpaRepository prontuarioRepository;

    @Autowired
    private AtendimentoJpaRepository atendimentoRepository;

    @Autowired
    private SolicitacaoDeExameJpaRepository exameRepository;

    @Autowired
    private EntityManager entityManager;

    private AtendimentoEntity criarAtendimento() {
        ProntuarioEntity prontuario =
            prontuarioRepository.saveAndFlush(
                new ProntuarioEntity(500L)
            );

        return atendimentoRepository.inserir(
            new AtendimentoEntity(
                prontuario.getId(),
                900L,
                "Dor abdominal",
                "Paciente consciente",
                "Solicitar exames",
                Instant.parse("2026-11-19T10:00:00Z")
            )
        );
    }

    @Test
    void deveCriarExameComStatusSolicitado() {
        AtendimentoEntity atendimento = criarAtendimento();

        SolicitacaoDeExameEntity exame =
            exameRepository.save(
                new SolicitacaoDeExameEntity(
                    atendimento.getId()
                )
            );

        entityManager.flush();
        entityManager.clear();

        var encontrado =
            exameRepository.findById(exame.getId())
                .orElseThrow();

        assertEquals(
            StatusExame.SOLICITADO,
            encontrado.getStatus()
        );

        assertEquals(
            atendimento.getId(),
            encontrado.getAtendimentoId()
        );
    }

    @Test
    void deveAtualizarSomenteStatusDoExame() {
        AtendimentoEntity atendimento = criarAtendimento();

        SolicitacaoDeExameEntity exame =
            exameRepository.save(
                new SolicitacaoDeExameEntity(
                    atendimento.getId()
                )
            );

        entityManager.flush();

        int atualizados = exameRepository.atualizarStatus(
            exame.getId(),
            "SOLICITADO",
            "REALIZADO"
        );

        assertEquals(1, atualizados);

        var atualizado =
            exameRepository.findById(exame.getId())
                .orElseThrow();

        assertEquals(
            StatusExame.REALIZADO,
            atualizado.getStatus()
        );

        assertEquals(
            atendimento.getId(),
            atualizado.getAtendimentoId()
        );
    }

    @Test
    void naoAtualizaQuandoStatusAnteriorNaoConfere() {
        AtendimentoEntity atendimento = criarAtendimento();

        SolicitacaoDeExameEntity exame =
            exameRepository.save(
                new SolicitacaoDeExameEntity(
                    atendimento.getId()
                )
            );

        entityManager.flush();

        int atualizados = exameRepository.atualizarStatus(
            exame.getId(),
            "REALIZADO",
            "CANCELADO"
        );

        assertEquals(0, atualizados);

        var encontrado =
            exameRepository.findById(exame.getId())
                .orElseThrow();

        assertEquals(
            StatusExame.SOLICITADO,
            encontrado.getStatus()
        );
    }

    @Test
    void naoPermiteExameSemAtendimento() {
        SolicitacaoDeExameEntity exame =
            new SolicitacaoDeExameEntity(999999L);

        assertThrows(
    DataIntegrityViolationException.class,
    () -> {
        exameRepository.save(exame);
        entityManager.flush();
    }
);
    }
}
