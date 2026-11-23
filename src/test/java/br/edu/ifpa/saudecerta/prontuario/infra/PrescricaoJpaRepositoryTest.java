
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.time.Instant;

import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
    AtendimentoJpaRepository.class,
    PrescricaoJpaRepository.class,
    ItemDePrescricaoJpaRepository.class
})
class PrescricaoJpaRepositoryTest {

    @Autowired
    private ProntuarioJpaRepository prontuarioRepository;

    @Autowired
    private AtendimentoJpaRepository atendimentoRepository;

    @Autowired
    private PrescricaoJpaRepository prescricaoRepository;

    @Autowired
    private ItemDePrescricaoJpaRepository itemRepository;

    @Autowired
    private EntityManager entityManager;

    private AtendimentoEntity criarAtendimento() {
        ProntuarioEntity prontuario =
            prontuarioRepository.saveAndFlush(
                new ProntuarioEntity(400L)
            );

        return atendimentoRepository.inserir(
            new AtendimentoEntity(
                prontuario.getId(),
                800L,
                "Dor de garganta",
                "Paciente consciente",
                "Tratamento medicamentoso",
                Instant.parse("2026-11-19T10:00:00Z")
            )
        );
    }

    @Test
    void deveRegistrarPrescricaoComMedicamentos() {
        AtendimentoEntity atendimento = criarAtendimento();

        PrescricaoEntity prescricao =
            prescricaoRepository.inserir(
                new PrescricaoEntity(
                    atendimento.getId(),
                    Instant.parse("2026-11-19T11:00:00Z")
                )
            );

        itemRepository.inserir(
            new ItemDePrescricaoEntity(
                prescricao.getId(),
                "Paracetamol",
                "500mg",
                "A cada 8 horas",
                "3 dias"
            )
        );

        itemRepository.inserir(
            new ItemDePrescricaoEntity(
                prescricao.getId(),
                "Dipirona",
                "500mg",
                "A cada 6 horas",
                "2 dias"
            )
        );

        entityManager.clear();

        var prescricoes =
            prescricaoRepository.buscarPorAtendimento(
                atendimento.getId()
            );

        var itens =
            itemRepository.buscarPorPrescricao(
                prescricao.getId()
            );

        assertEquals(1, prescricoes.size());
        assertEquals(2, itens.size());

        assertEquals(
            "Paracetamol",
            itens.get(0).getMedicamento()
        );

        assertEquals(
            "Dipirona",
            itens.get(1).getMedicamento()
        );

        assertEquals(
            atendimento.getId(),
            prescricoes.get(0).getAtendimentoId()
        );
    }

    @Test
    void naoPermitePrescricaoSemAtendimento() {
        PrescricaoEntity prescricao =
            new PrescricaoEntity(
                999999L,
                Instant.now()
            );

        assertThrows(
            ConstraintViolationException.class,
            () -> prescricaoRepository.inserir(prescricao)
        );
    }

    @Test
    void naoPermiteMedicamentoSemPrescricao() {
        ItemDePrescricaoEntity item =
            new ItemDePrescricaoEntity(
                999999L,
                "Paracetamol",
                "500mg",
                "A cada 8 horas",
                "3 dias"
            );

        assertThrows(
            ConstraintViolationException.class,
            () -> itemRepository.inserir(item)
        );
    }
}
