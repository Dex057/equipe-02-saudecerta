
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.time.Instant;

import br.edu.ifpa.saudecerta.prontuario.domain.Atendimento;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;

import jakarta.persistence.EntityManager;

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
class AtendimentoRepositoryAdapterTest {

    @Autowired
    private ProntuarioJpaRepository prontuarioRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveSalvarERecuperarAtendimentoDoDominio() {

        ProntuarioEntity prontuario =
            prontuarioRepository.saveAndFlush(
                new ProntuarioEntity(600L)
            );

        Atendimento atendimento = new Atendimento(
            null,
            prontuario.getId(),
            1000L,
            "Dor abdominal",
            "Paciente consciente",
            "Solicitar exames",
            Instant.parse("2026-11-19T10:00:00Z")
        );

        Atendimento salvo =
            atendimentoRepository.inserir(atendimento);

        assertNotNull(salvo.getId());

        entityManager.clear();

        Atendimento encontrado =
            atendimentoRepository.buscarPorId(
                salvo.getId()
            ).orElseThrow();

        assertEquals(
            salvo.getId(),
            encontrado.getId()
        );

        assertEquals(
            "Dor abdominal",
            encontrado.getQueixa()
        );

        assertEquals(
            "Paciente consciente",
            encontrado.getEvolucao()
        );

        assertEquals(
            "Solicitar exames",
            encontrado.getConduta()
        );

        assertEquals(
            1000L,
            encontrado.getConsultaId()
        );
    }

    @Test
    void naoPermiteReinserirAtendimentoComId() {

        Atendimento atendimento = new Atendimento(
            999L,
            1L,
            1001L,
            "Dor de cabeça",
            "Paciente consciente",
            "Repouso",
            Instant.now()
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> atendimentoRepository.inserir(atendimento)
        );
    }
}
