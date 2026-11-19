
package br.edu.ifpa.saudecerta.prontuario.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProntuarioJpaRepositoryTest {

    @Autowired
    private ProntuarioJpaRepository repository;

    @Test
    void deveSalvarEBuscarProntuarioPorPaciente() {
        ProntuarioEntity prontuario =
                new ProntuarioEntity(100L);

        repository.saveAndFlush(prontuario);

        var resultado = repository.findByPacienteId(100L);

        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getId());
        assertEquals(100L, resultado.get().getPacienteId());
    }

    @Test
    void deveRetornarVazioParaPacienteSemProntuario() {
        var resultado = repository.findByPacienteId(999999L);

        assertTrue(resultado.isEmpty());
    }
}
