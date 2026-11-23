
package br.edu.ifpa.saudecerta.prontuario.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PrescricaoTest {

    private ItemDePrescricao criarItem() {
        return new ItemDePrescricao(
            1L,
            "Medicamento de teste",
            "500 mg",
            "A cada 8 horas",
            "5 dias"
        );
    }

    @Test
    void deveCriarPrescricaoValida() {
        Prescricao prescricao = new Prescricao(
            1L, 10L, List.of(criarItem()), Instant.now()
        );

        assertEquals(10L, prescricao.getAtendimentoId());
        assertEquals(1, prescricao.getItens().size());
    }

    @Test
    void exigeAtendimentoValido() {
        assertThrows(IllegalArgumentException.class,
            () -> new Prescricao(
                1L, null, List.of(criarItem()), Instant.now()
            )
        );
    }

    @Test
    void naoPermitePrescricaoSemItens() {
        assertThrows(IllegalArgumentException.class,
            () -> new Prescricao(
                1L, 10L, List.of(), Instant.now()
            )
        );
    }

    @Test
    void naoPermiteAlterarListaDaPrescricao() {
        Prescricao prescricao = new Prescricao(
            1L, 10L, List.of(criarItem()), Instant.now()
        );

        assertThrows(UnsupportedOperationException.class,
            () -> prescricao.getItens().clear()
        );
    }

    @Test
    void copiaListaParaProtegerItens() {
        List<ItemDePrescricao> itens = new ArrayList<>();
        itens.add(criarItem());

        Prescricao prescricao = new Prescricao(
            1L, 10L, itens, Instant.now()
        );

        itens.clear();

        assertEquals(1, prescricao.getItens().size());
    }

    @Test
    void naoPermiteMedicamentoVazio() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemDePrescricao(
                1L, " ", "500 mg", "A cada 8 horas", "5 dias"
            )
        );
    }
}
