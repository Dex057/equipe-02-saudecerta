
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Repository
public class ComplementoJpaRepository {

    private final EntityManager entityManager;

    public ComplementoJpaRepository(
            EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public ComplementoEntity inserir(
            ComplementoEntity complemento) {

        Objects.requireNonNull(
            complemento, "Complemento obrigatorio."
        );

        entityManager.persist(complemento);
        entityManager.flush();

        return complemento;
    }

    @Transactional(readOnly = true)
    public Optional<ComplementoEntity> buscarPorId(Long id) {

        return Optional.ofNullable(
            entityManager.find(ComplementoEntity.class, id)
        );
    }

    @Transactional(readOnly = true)
    public List<ComplementoEntity> buscarPorAtendimento(
            Long atendimentoId) {

        return entityManager.createQuery(
            "SELECT c FROM ComplementoEntity c " +
            "WHERE c.atendimentoId = :atendimentoId " +
            "ORDER BY c.dataHora ASC, c.id ASC",
            ComplementoEntity.class
        )
        .setParameter("atendimentoId", atendimentoId)
        .getResultList();
    }
}
