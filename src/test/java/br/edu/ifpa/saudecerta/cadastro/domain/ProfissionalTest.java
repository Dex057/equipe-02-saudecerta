package br.edu.ifpa.saudecerta.cadastro.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProfissionalTest {

    @Test
    void exigePeloMenosUmaEspecialidade() {
        assertThatThrownBy(() -> Profissional.novo("Dra. Lia", List.of()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("especialidade");
        assertThatThrownBy(() -> Profissional.novo("Dra. Lia", null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void guardaEspecialidadesSemRepetir() {
        Profissional p = Profissional.novo("Dra. Lia", List.of(2L, 1L, 2L));
        assertThat(p.especialidadeIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(p.ativo()).isTrue();
    }

    @Test
    void atualizarSemEspecialidadeNaoDesfazOEstadoAnterior() {
        Profissional p = Profissional.novo("Dra. Lia", List.of(1L));
        assertThatThrownBy(() -> p.atualizar("Outro", List.of())).isInstanceOf(RegraDeNegocioException.class);
        assertThat(p.nome()).isEqualTo("Dra. Lia");
        assertThat(p.especialidadeIds()).containsExactly(1L);
    }
}
