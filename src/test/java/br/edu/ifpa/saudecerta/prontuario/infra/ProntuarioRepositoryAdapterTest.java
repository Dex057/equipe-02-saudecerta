
package br.edu.ifpa.saudecerta.prontuario.infra;

import br.edu.ifpa.saudecerta.prontuario.domain.ProntuarioRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(ProntuarioRepositoryAdapter.class)
class ProntuarioRepositoryAdapterTest {

    @Autowired
    private ProntuarioRepository prontuarioRepository;

    @Autowired
    private ProntuarioJpaRepository jpaRepository;

    @Test
    void deveCriarProntuarioParaPaciente() {

        Long id = prontuarioRepository
            .buscarOuCriarPorPaciente(700L);

        assertNotNull(id);

        var prontuario = jpaRepository
            .findByPacienteId(700L)
            .orElseThrow();

        assertEquals(id, prontuario.getId());
    }

    @Test
    void deveReutilizarProntuarioExistente() {

        Long primeiroId = prontuarioRepository
            .buscarOuCriarPorPaciente(701L);

        Long segundoId = prontuarioRepository
            .buscarOuCriarPorPaciente(701L);

        assertEquals(primeiroId, segundoId);

        assertEquals(
            1,
            jpaRepository.count()
        );
    }

    @Test
    void naoPermitePacienteInvalido() {

        assertThrows(
            IllegalArgumentException.class,
            () -> prontuarioRepository
                .buscarOuCriarPorPaciente(-1L)
        );
    }
    
@Test
void naoCriaProntuarioAoConsultarPacienteSemRegistro() {

    long quantidadeAntes = jpaRepository.count();

    var encontrado = prontuarioRepository
        .buscarIdPorPaciente(703L);

    assertTrue(encontrado.isEmpty());

    assertEquals(
        quantidadeAntes,
        jpaRepository.count()
    );
}

@Test
void deveEncontrarProntuarioExistentePorPaciente() {

    Long prontuarioId = prontuarioRepository
        .buscarOuCriarPorPaciente(702L);

    var encontrado = prontuarioRepository
        .buscarIdPorPaciente(702L);

    assertTrue(encontrado.isPresent());

    assertEquals(
        prontuarioId,
        encontrado.orElseThrow()
    );
}

}
