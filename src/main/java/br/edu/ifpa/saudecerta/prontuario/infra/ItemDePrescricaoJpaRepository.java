
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Repository
public class ItemDePrescricaoJpaRepository {

    private final EntityManager entityManager;

    public ItemDePrescricaoJpaRepository(
            EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public ItemDePrescricaoEntity inserir(
            ItemDePrescricaoEntity item) {

        Objects.requireNonNull(
            item, "Item de prescricao obrigatorio."
        );

        entityManager.persist(item);
        entityManager.flush();

        return item;
    }

    @Transactional(readOnly = true)
    public Optional<ItemDePrescricaoEntity> buscarPorId(
            Long id) {

        return Optional.ofNullable(
            entityManager.find(
                ItemDePrescricaoEntity.class, id
            )
        );
    }

    @Transactional(readOnly = true)
    public List<ItemDePrescricaoEntity> buscarPorPrescricao(
            Long prescricaoId) {

        return entityManager.createQuery(
            "SELECT i FROM ItemDePrescricaoEntity i " +
            "WHERE i.prescricaoId = :prescricaoId " +
            "ORDER BY i.id ASC",
            ItemDePrescricaoEntity.class
        )
        .setParameter("prescricaoId", prescricaoId)
        .getResultList();
    }
}
