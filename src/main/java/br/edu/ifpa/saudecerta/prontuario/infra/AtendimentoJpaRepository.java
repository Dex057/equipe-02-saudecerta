
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.List;
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

    @Transactional(readOnly = true)
    public List<AtendimentoEntity> buscarPaginaPorProntuario(
            Long prontuarioId,
            int pagina,
            int tamanho) {

        validarPaginacao(prontuarioId, pagina, tamanho);

        return entityManager.createQuery(
            "SELECT a FROM AtendimentoEntity a " +
            "WHERE a.prontuarioId = :prontuarioId " +
            "ORDER BY a.dataHora DESC, a.id DESC",
            AtendimentoEntity.class
        )
        .setParameter("prontuarioId", prontuarioId)
        .setFirstResult(Math.multiplyExact(pagina, tamanho))
        .setMaxResults(tamanho)
        .getResultList();
    }

    @Transactional(readOnly = true)
    public long contarPorProntuario(Long prontuarioId) {

        if (prontuarioId == null || prontuarioId <= 0) {
            throw new IllegalArgumentException(
                "Prontuario invalido."
            );
        }

        return entityManager.createQuery(
            "SELECT COUNT(a) FROM AtendimentoEntity a " +
            "WHERE a.prontuarioId = :prontuarioId",
            Long.class
        )
        .setParameter("prontuarioId", prontuarioId)
        .getSingleResult();
    }

    private void validarPaginacao(
            Long prontuarioId,
            int pagina,
            int tamanho) {

        if (prontuarioId == null || prontuarioId <= 0) {
            throw new IllegalArgumentException(
                "Prontuario invalido."
            );
        }

        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new IllegalArgumentException(
                "Parametros de paginacao invalidos."
            );
        }
    }
}
