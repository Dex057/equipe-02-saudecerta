
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AtendimentoJpaRepository.class)
class AtendimentoPaginacaoJpaTest {

    @Autowired
    private ProntuarioJpaRepository prontuarioRepository;

    @Autowired
    private AtendimentoJpaRepository atendimentoRepository;

    private void criarAtendimentos(
            Long prontuarioId,
            int quantidade,
            long consultaInicial) {

        Instant inicio =
            Instant.parse("2026-11-21T10:00:00Z");

        for (int i = 0; i < quantidade; i++) {

            atendimentoRepository.inserir(
                new AtendimentoEntity(
                    prontuarioId,
                    consultaInicial + i,
                    "Queixa " + i,
                    "Evolucao " + i,
                    "Conduta " + i,
                    inicio.plusSeconds(i * 60L)
                )
            );
        }
    }

    @Test
    void devePaginarDoMaisAntigoParaOMaisRecente() {

        Long prontuarioId = prontuarioRepository
            .saveAndFlush(new ProntuarioEntity(800L))
            .getId();

        criarAtendimentos(prontuarioId, 12, 1300L);

        var pagina0 =
            atendimentoRepository.buscarPaginaPorProntuario(
                prontuarioId, 0, 5
            );

        var pagina1 =
            atendimentoRepository.buscarPaginaPorProntuario(
                prontuarioId, 1, 5
            );

        var pagina2 =
            atendimentoRepository.buscarPaginaPorProntuario(
                prontuarioId, 2, 5
            );

        assertEquals(5, pagina0.size());
        assertEquals(5, pagina1.size());
        assertEquals(2, pagina2.size());

        assertEquals(1300L, pagina0.get(0).getConsultaId());
        assertEquals(1305L, pagina1.get(0).getConsultaId());
        assertEquals(1311L, pagina2.get(1).getConsultaId());

        assertEquals(
            12L,
            atendimentoRepository.contarPorProntuario(
                prontuarioId
            )
        );
    }

    @Test
    void naoMisturaAtendimentosDeProntuariosDiferentes() {

        Long primeiro = prontuarioRepository
            .saveAndFlush(new ProntuarioEntity(801L))
            .getId();

        Long segundo = prontuarioRepository
            .saveAndFlush(new ProntuarioEntity(802L))
            .getId();

        criarAtendimentos(primeiro, 3, 1400L);
        criarAtendimentos(segundo, 2, 1500L);

        var resultados =
            atendimentoRepository.buscarPaginaPorProntuario(
                primeiro, 0, 10
            );

        assertEquals(3, resultados.size());

        assertTrue(
            resultados.stream().allMatch(
                a -> a.getProntuarioId().equals(primeiro)
            )
        );

        assertEquals(
            3L,
            atendimentoRepository.contarPorProntuario(
                primeiro
            )
        );
    }

    @Test
    void rejeitaParametrosDePaginacaoInvalidos() {

        assertThrows(
            IllegalArgumentException.class,
            () -> atendimentoRepository
                .buscarPaginaPorProntuario(1L, -1, 10)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> atendimentoRepository
                .buscarPaginaPorProntuario(1L, 0, 0)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> atendimentoRepository
                .buscarPaginaPorProntuario(1L, 0, 101)
        );
    }
}
