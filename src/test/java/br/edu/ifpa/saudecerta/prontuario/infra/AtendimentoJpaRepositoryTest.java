
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.hibernate.exception.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AtendimentoJpaRepository.class)
class AtendimentoJpaRepositoryTest {

    @Autowired
    private AtendimentoJpaRepository atendimentoRepository;

    @Autowired
    private ProntuarioJpaRepository prontuarioRepository;

    @Test
    void deveSalvarEBuscarAtendimento() {

        ProntuarioEntity prontuario =
            prontuarioRepository.saveAndFlush(
                new ProntuarioEntity(200L)
            );

        AtendimentoEntity atendimento =
            new AtendimentoEntity(
                prontuario.getId(),
                450L,
                "Dor de cabeça",
                "Paciente consciente e orientado",
                "Repouso e acompanhamento",
                Instant.now()
            );

        atendimentoRepository.inserir(atendimento);

        var resultado =
            atendimentoRepository.buscarPorId(
                atendimento.getId()
            );

        assertTrue(resultado.isPresent());
        assertEquals(
            450L,
            resultado.get().getConsultaId()
        );
        assertEquals(
            prontuario.getId(),
            resultado.get().getProntuarioId()
        );
    }

    @Test
    void naoPermiteAtendimentoSemProntuarioExistente() {

        AtendimentoEntity atendimento =
            new AtendimentoEntity(
                999999L,
                451L,
                "Dor abdominal",
                "Paciente estável",
                "Solicitar exames",
                Instant.now()
            );

        assertThrows(
    ConstraintViolationException.class,
    () -> atendimentoRepository.inserir(atendimento)
);
    }
}
