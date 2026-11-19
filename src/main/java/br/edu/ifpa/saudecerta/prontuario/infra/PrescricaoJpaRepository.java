
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Repository
public class PrescricaoJpaRepository {

    private final EntityManager entityManager;

    public PrescricaoJpaRepository(
            EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public PrescricaoEntity inserir(
            PrescricaoEntity prescricao) {

        Objects.requireNonNull(
            prescricao, "Prescricao obrigatoria."
        );

        entityManager.persist(prescricao);
        entityManager.flush();

        return prescricao;
    }

    @Transactional(readOnly = true)
    public Optional<PrescricaoEntity> buscarPorId(Long id) {

        return Optional.ofNullable(
            entityManager.find(PrescricaoEntity.class, id)
        );
    }

    @Transactional(readOnly = true)
    public List<PrescricaoEntity> buscarPorAtendimento(
            Long atendimentoId) {

        return entityManager.createQuery(
            "SELECT p FROM PrescricaoEntity p " +
            "WHERE p.atendimentoId = :atendimentoId " +
            "ORDER BY p.dataHora ASC, p.id ASC",
            PrescricaoEntity.class
        )
        .setParameter("atendimentoId", atendimentoId)
        .getResultList();
    }
}

