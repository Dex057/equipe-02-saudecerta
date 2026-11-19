
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.time.Instant;

import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
    AtendimentoJpaRepository.class,
    ComplementoJpaRepository.class
})
class ComplementoJpaRepositoryTest {

    @Autowired
    private ProntuarioJpaRepository prontuarioRepository;

    @Autowired
    private AtendimentoJpaRepository atendimentoRepository;

    @Autowired
    private ComplementoJpaRepository complementoRepository;

    @Autowired
    private EntityManager entityManager;

    private AtendimentoEntity criarAtendimento() {
        ProntuarioEntity prontuario =
            prontuarioRepository.saveAndFlush(
                new ProntuarioEntity(300L)
            );

        return atendimentoRepository.inserir(
            new AtendimentoEntity(
                prontuario.getId(),
                700L,
                "Dor de cabeça",
                "Paciente consciente",
                "Repouso",
                Instant.parse("2026-11-19T10:00:00Z")
            )
        );
    }

    @Test
    void deveInserirComplementoSemAlterarAtendimento() {
        AtendimentoEntity atendimento = criarAtendimento();

        ComplementoEntity complemento =
            complementoRepository.inserir(
                new ComplementoEntity(
                    atendimento.getId(),
                    50L,
                    "Paciente apresentou melhora.",
                    Instant.parse("2026-11-19T12:00:00Z")
                )
            );

        entityManager.clear();

        AtendimentoEntity original =
            atendimentoRepository.buscarPorId(
                atendimento.getId()
            ).orElseThrow();

        assertEquals("Dor de cabeça", original.getQueixa());
        assertEquals(
            "Paciente consciente", original.getEvolucao()
        );
        assertEquals("Repouso", original.getConduta());

        assertTrue(
            complementoRepository.buscarPorId(
                complemento.getId()
            ).isPresent()
        );
    }

    @Test
    void deveListarComplementosEmOrdemCronologica() {
        AtendimentoEntity atendimento = criarAtendimento();

        complementoRepository.inserir(
            new ComplementoEntity(
                atendimento.getId(),
                50L,
                "Complemento mais recente",
                Instant.parse("2026-11-19T15:00:00Z")
            )
        );

        complementoRepository.inserir(
            new ComplementoEntity(
                atendimento.getId(),
                50L,
                "Complemento mais antigo",
                Instant.parse("2026-11-19T11:00:00Z")
            )
        );

        entityManager.clear();

        var complementos =
            complementoRepository.buscarPorAtendimento(
                atendimento.getId()
            );

        assertEquals(2, complementos.size());
        assertEquals(
            "Complemento mais antigo",
            complementos.get(0).getTexto()
        );
        assertEquals(
            "Complemento mais recente",
            complementos.get(1).getTexto()
        );
    }

    @Test
    void naoPermiteComplementoSemAtendimentoExistente() {
        ComplementoEntity complemento =
            new ComplementoEntity(
                999999L,
                50L,
                "Complemento inválido",
                Instant.now()
            );

        assertThrows(
            ConstraintViolationException.class,
            () -> complementoRepository.inserir(complemento)
        );
    }
}
