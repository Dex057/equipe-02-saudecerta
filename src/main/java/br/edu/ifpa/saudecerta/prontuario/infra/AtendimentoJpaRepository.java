
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Repository
public class AtendimentoJpaRepository {

    private final EntityManager entityManager;

    public AtendimentoJpaRepository(
            EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public AtendimentoEntity inserir(
            AtendimentoEntity atendimento) {

        Objects.requireNonNull(
            atendimento, "Atendimento obrigatorio."
        );

        entityManager.persist(atendimento);
        entityManager.flush();

        return atendimento;
    }

    @Transactional(readOnly = true)
    public Optional<AtendimentoEntity> buscarPorId(Long id) {

        return Optional.ofNullable(
            entityManager.find(AtendimentoEntity.class, id)
        );
    }
}
