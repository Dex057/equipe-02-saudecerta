package br.edu.ifpa.saudecerta.prontuario.infra;

import java.time.Instant;

import br.edu.ifpa.saudecerta.prontuario.domain.PrescricaoLeituraRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
    AtendimentoJpaRepository.class,
    PrescricaoJpaRepository.class,
    ItemDePrescricaoJpaRepository.class,
    PrescricaoLeituraRepositoryAdapter.class
})
class PrescricaoLeituraRepositoryAdapterTest {

    @Autowired ProntuarioJpaRepository prontuarios;
    @Autowired AtendimentoJpaRepository atendimentos;
    @Autowired PrescricaoJpaRepository prescricoes;
    @Autowired ItemDePrescricaoJpaRepository itens;
    @Autowired PrescricaoLeituraRepository leitura;

    @Test
    void recuperaPrescricaoComItens() {
        var prontuario = prontuarios.saveAndFlush(new ProntuarioEntity(990L));
        var atendimento = atendimentos.inserir(new AtendimentoEntity(
                prontuario.getId(), 880L, "Dor", "Consciente",
                "Conduta", Instant.parse("2026-11-21T10:00:00Z")));
        var prescricao = prescricoes.inserir(new PrescricaoEntity(
                atendimento.getId(), Instant.parse("2026-11-21T10:05:00Z")));
        itens.inserir(new ItemDePrescricaoEntity(
                prescricao.getId(), "Dipirona", "500mg", "8/8h", "3 dias"));

        var resultado = leitura.buscarPorAtendimento(atendimento.getId());
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getItens().size());
        assertEquals("Dipirona", resultado.get(0).getItens().get(0).getMedicamento());
    }
}
