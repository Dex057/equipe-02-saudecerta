package br.edu.ifpa.saudecerta.cadastro.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CpfTest {

    @Test
    void aceitaComEmSemPontuacaoENormalizaPara11Digitos() {
        assertThat(Cpf.de("529.982.247-25").numero()).isEqualTo("52998224725");
        assertThat(Cpf.de("52998224725")).isEqualTo(Cpf.de("529.982.247-25"));
    }

    @Test
    void recusaDigitoVerificadorErrado() {
        assertThatThrownBy(() -> Cpf.de("529.982.247-24")).isInstanceOf(CpfInvalidoException.class);
    }

    @Test
    void recusaSequenciaRepetidaTamanhoErradoENulo() {
        assertThatThrownBy(() -> Cpf.de("111.111.111-11")).isInstanceOf(CpfInvalidoException.class);
        assertThatThrownBy(() -> Cpf.de("123")).isInstanceOf(CpfInvalidoException.class);
        assertThatThrownBy(() -> Cpf.de(null)).isInstanceOf(CpfInvalidoException.class);
        assertThatThrownBy(() -> Cpf.de("  ")).isInstanceOf(CpfInvalidoException.class);
    }
}
