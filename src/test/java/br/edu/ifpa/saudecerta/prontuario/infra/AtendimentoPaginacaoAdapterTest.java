
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.time.Instant;

import br.edu.ifpa.saudecerta.prontuario.domain.Atendimento;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
    AtendimentoJpaRepository.class,
    AtendimentoRepositoryAdapter.class
})
class AtendimentoPaginacaoAdapterTest {

    @Autowired
    private ProntuarioJpaRepository prontuarioRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Test
    void devePaginarAtendimentosPeloDominio() {

        Long prontuarioId = prontuarioRepository
            .saveAndFlush(new ProntuarioEntity(900L))
            .getId();

        atendimentoRepository.inserir(
            new Atendimento(
                null,
                prontuarioId,
                2000L,
                "Primeira queixa",
                "Primeira evolucao",
                "Primeira conduta",
                Instant.parse("2026-11-20T10:00:00Z")
            )
        );

        atendimentoRepository.inserir(
            new Atendimento(
                null,
                prontuarioId,
                2001L,
                "Segunda queixa",
                "Segunda evolucao",
                "Segunda conduta",
                Instant.parse("2026-11-21T10:00:00Z")
            )
        );

        var primeiraPagina =
            atendimentoRepository.buscarPaginaPorProntuario(
                prontuarioId, 0, 1
            );

        var segundaPagina =
            atendimentoRepository.buscarPaginaPorProntuario(
                prontuarioId, 1, 1
            );

        assertEquals(1, primeiraPagina.size());
        assertEquals(1, segundaPagina.size());

        assertEquals(
            "Segunda queixa",
            primeiraPagina.get(0).getQueixa()
        );

        assertEquals(
            "Primeira queixa",
            segundaPagina.get(0).getQueixa()
        );

        assertEquals(
            2L,
            atendimentoRepository.contarPorProntuario(
                prontuarioId
            )
        );

        assertTrue(
            atendimentoRepository.buscarPaginaPorProntuario(
                prontuarioId, 2, 1
            ).isEmpty()
        );
    }
}
