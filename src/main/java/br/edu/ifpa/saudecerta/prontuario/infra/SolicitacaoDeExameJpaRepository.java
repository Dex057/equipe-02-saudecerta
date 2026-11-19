
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SolicitacaoDeExameJpaRepository
        extends Repository<SolicitacaoDeExameEntity, Long> {

    // Usar somente para inserir uma nova solicitacao.
    SolicitacaoDeExameEntity save(
            SolicitacaoDeExameEntity exame);

    Optional<SolicitacaoDeExameEntity> findById(Long id);

    List<SolicitacaoDeExameEntity>
        findByAtendimentoIdOrderByIdAsc(Long atendimentoId);

    @Transactional
    @Modifying(
        flushAutomatically = true,
        clearAutomatically = true
    )
    @Query(
        value = """
            UPDATE pro_solicitacao_exame
            SET status = :novoStatus
            WHERE id = :id
              AND status = :statusAnterior
            """,
        nativeQuery = true
    )
    int atualizarStatus(
            @Param("id") Long id,
            @Param("statusAnterior") String statusAnterior,
            @Param("novoStatus") String novoStatus
    );
}
