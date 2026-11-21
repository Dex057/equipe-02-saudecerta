
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.time.Instant;

import br.edu.ifpa.saudecerta.prontuario.domain.Complemento;
import br.edu.ifpa.saudecerta.prontuario.domain.ComplementoRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
    AtendimentoJpaRepository.class,
    ComplementoJpaRepository.class,
    ComplementoRepositoryAdapter.class
})
class ComplementoRepositoryAdapterTest {

    @Autowired
    private ProntuarioJpaRepository prontuarioRepository;

    @Autowired
    private AtendimentoJpaRepository atendimentoRepository;

    @Autowired
    private ComplementoRepository complementoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveSalvarERecuperarComplementoDoDominio() {

        ProntuarioEntity prontuario =
            prontuarioRepository.saveAndFlush(
                new ProntuarioEntity(700L)
            );

        AtendimentoEntity atendimento =
            atendimentoRepository.inserir(
                new AtendimentoEntity(
                    prontuario.getId(),
                    1100L,
                    "Dor abdominal",
                    "Paciente consciente",
                    "Solicitar exames",
                    Instant.parse("2026-11-21T10:00:00Z")
                )
            );

        Complemento complemento = new Complemento(
            null,
            atendimento.getId(),
            50L,
            "Paciente apresentou melhora.",
            Instant.parse("2026-11-21T12:00:00Z")
        );

        Complemento salvo =
            complementoRepository.inserir(complemento);

        assertNotNull(salvo.getId());

        entityManager.clear();

        Complemento encontrado =
            complementoRepository.buscarPorId(
                salvo.getId()
            ).orElseThrow();

        assertEquals(
            "Paciente apresentou melhora.",
            encontrado.getTexto()
        );

        assertEquals(
            atendimento.getId(),
            encontrado.getAtendimentoId()
        );

        assertEquals(50L, encontrado.getProfissionalId());

        var complementos =
            complementoRepository.buscarPorAtendimento(
                atendimento.getId()
            );

        assertEquals(1, complementos.size());
        assertEquals(salvo.getId(), complementos.get(0).getId());
    }

    @Test
    void naoPermiteReinserirComplementoComId() {

        Complemento complemento = new Complemento(
            999L,
            1L,
            50L,
            "Complemento existente",
            Instant.now()
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> complementoRepository.inserir(complemento)
        );
    }
}
