package br.edu.ifpa.saudecerta.cadastro;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.ifpa.saudecerta.cadastro.application.EspecialidadeService;
import br.edu.ifpa.saudecerta.cadastro.application.PacienteDados;
import br.edu.ifpa.saudecerta.cadastro.application.PacienteService;
import br.edu.ifpa.saudecerta.cadastro.application.ProfissionalDados;
import br.edu.ifpa.saudecerta.cadastro.application.ProfissionalService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** A interface pública que Agenda e Prontuário vão consumir devolve só os resumos. */
@SpringBootTest
class CadastroApiServiceTest {

    @Autowired CadastroApi api;
    @Autowired PacienteService pacientes;
    @Autowired ProfissionalService profissionais;
    @Autowired EspecialidadeService especialidades;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void limpa() {
        jdbc.update("delete from cad_profissional_especialidade");
        jdbc.update("delete from cad_profissional");
        jdbc.update("delete from cad_especialidade");
        jdbc.update("delete from cad_paciente");
    }

    @Test
    void buscaPacienteEProfissionalPorId() {
        Long pacienteId = pacientes.cadastrar(new PacienteDados("Ana Souza", "529.982.247-25",
                LocalDate.of(1990, 4, 15), null, null, null)).id();
        Long especialidadeId = especialidades.cadastrar("Cardiologia").id();
        Long profissionalId = profissionais.cadastrar(
                new ProfissionalDados("Dr. Caio", List.of(especialidadeId), null)).id();

        assertThat(api.buscarPaciente(pacienteId)).contains(new PacienteResumo(pacienteId, "Ana Souza", true));
        assertThat(api.buscarProfissional(profissionalId))
                .contains(new ProfissionalResumo(profissionalId, "Dr. Caio", List.of(especialidadeId), true));
    }

    @Test
    void idInexistenteVoltaVazio() {
        assertThat(api.buscarPaciente(999_999L)).isEmpty();
        assertThat(api.buscarProfissional(999_999L)).isEmpty();
    }
}
