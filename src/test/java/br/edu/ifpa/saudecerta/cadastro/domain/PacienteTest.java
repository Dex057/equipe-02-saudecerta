package br.edu.ifpa.saudecerta.cadastro.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PacienteTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 18);
    private static final Cpf CPF = Cpf.de("529.982.247-25");

    @Test
    void nascePacienteAtivoComNomeSemEspacosNasPontas() {
        Paciente p = Paciente.novo("  Ana Souza ", CPF, LocalDate.of(1990, 4, 15), null, null, HOJE);
        assertThat(p.ativo()).isTrue();
        assertThat(p.nome()).isEqualTo("Ana Souza");
        assertThat(p.id()).isNull();
    }

    @Test
    void recusaNascimentoNoFuturoENomeEmBranco() {
        assertThatThrownBy(() -> Paciente.novo("Ana", CPF, HOJE.plusDays(1), null, null, HOJE))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Paciente.novo(" ", CPF, HOJE.minusYears(30), null, null, HOJE))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void inativaEReativa() {
        Paciente p = Paciente.novo("Ana", CPF, HOJE.minusYears(30), null, null, HOJE);
        p.inativar();
        assertThat(p.ativo()).isFalse();
        p.reativar();
        assertThat(p.ativo()).isTrue();
    }

    @Test
    void normalizaNomeSemAcentoEmMinusculas() {
        assertThat(Paciente.normalizar("  JOSÉ Conceição ")).isEqualTo("jose conceicao");
        assertThat(Paciente.normalizar(null)).isEmpty();
    }
}
