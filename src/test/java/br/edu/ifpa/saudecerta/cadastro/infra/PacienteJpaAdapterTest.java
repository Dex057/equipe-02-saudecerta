package br.edu.ifpa.saudecerta.cadastro.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.ifpa.saudecerta.cadastro.domain.Cpf;
import br.edu.ifpa.saudecerta.cadastro.domain.CpfDuplicadoException;
import br.edu.ifpa.saudecerta.cadastro.domain.Paciente;
import br.edu.ifpa.saudecerta.cadastro.domain.PacienteRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * RN01 na camada do banco (ADR-006): o repositório é chamado direto, sem a checagem prévia do serviço,
 * para provar que a restrição UNIQUE de cpf_ativo segura a corrida entre duas requisições.
 */
@SpringBootTest
class PacienteJpaAdapterTest {

    private static final LocalDate NASC = LocalDate.of(1990, 1, 1);
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 18);
    private static final Cpf CPF = Cpf.de("529.982.247-25");

    @Autowired
    PacienteRepository pacientes;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void limpa() {
        jdbc.update("delete from cad_paciente");
    }

    @Test
    void bancoRecusaSegundoPacienteAtivoComOMesmoCpf() {
        pacientes.salvar(Paciente.novo("Ana", CPF, NASC, null, null, HOJE));

        assertThatThrownBy(() -> pacientes.salvar(Paciente.novo("Outra Ana", CPF, NASC, null, null, HOJE)))
                .isInstanceOf(CpfDuplicadoException.class);
        assertThat(jdbc.queryForObject("select count(*) from cad_paciente", Integer.class)).isEqualTo(1);
    }

    @Test
    void inativoLiberaOCpfEFicaComCpfAtivoNulo() {
        Paciente ana = pacientes.salvar(Paciente.novo("Ana", CPF, NASC, null, null, HOJE));
        ana.inativar();
        pacientes.salvar(ana);

        assertThat(jdbc.queryForObject("select cpf_ativo from cad_paciente where id = ?", String.class, ana.id()))
                .isNull();
        // com o CPF liberado, outro paciente ativo pode ocupá-lo
        Paciente nova = pacientes.salvar(Paciente.novo("Ana Nova", CPF, NASC, null, null, HOJE));
        assertThat(pacientes.buscarAtivoPorCpf(CPF)).get().extracting(Paciente::id).isEqualTo(nova.id());
    }

    @Test
    void idasEVoltasPreservamOsDados() {
        Paciente gravado = pacientes.salvar(
                Paciente.novo("José Álvares", CPF, NASC, "91 99999-0000", "jose@example.test", HOJE));

        Paciente lido = pacientes.buscarPorId(gravado.id()).orElseThrow();
        assertThat(lido.nome()).isEqualTo("José Álvares");
        assertThat(lido.cpf()).isEqualTo(CPF);
        assertThat(lido.dataNascimento()).isEqualTo(NASC);
        assertThat(lido.telefone()).isEqualTo("91 99999-0000");
        assertThat(lido.email()).isEqualTo("jose@example.test");
        assertThat(lido.ativo()).isTrue();
    }
}
