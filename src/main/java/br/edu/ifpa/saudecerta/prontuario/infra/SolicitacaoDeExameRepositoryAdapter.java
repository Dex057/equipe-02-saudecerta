
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpa.saudecerta.prontuario.domain.SolicitacaoDeExame;
import br.edu.ifpa.saudecerta.prontuario.domain.SolicitacaoDeExameRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.StatusExame;

@Repository
public class SolicitacaoDeExameRepositoryAdapter
        implements SolicitacaoDeExameRepository {

    private final SolicitacaoDeExameJpaRepository jpaRepository;

    public SolicitacaoDeExameRepositoryAdapter(
            SolicitacaoDeExameJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public SolicitacaoDeExame inserir(
            SolicitacaoDeExame exame) {

        Objects.requireNonNull(exame, "Exame obrigatorio.");

        if (exame.getId() != null ||
                exame.getStatus() != StatusExame.SOLICITADO) {
            throw new IllegalArgumentException(
                "Somente exames novos podem ser inseridos."
            );
        }

        SolicitacaoDeExameEntity entity =
            new SolicitacaoDeExameEntity(
                exame.getAtendimentoId()
            );

        SolicitacaoDeExameEntity salvo =
            jpaRepository.save(entity);

        return paraDominio(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SolicitacaoDeExame> buscarPorId(Long id) {

        return jpaRepository.findById(id)
            .map(this::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitacaoDeExame> buscarPorAtendimento(
            Long atendimentoId) {

        return jpaRepository
            .findByAtendimentoIdOrderByIdAsc(atendimentoId)
            .stream()
            .map(this::paraDominio)
            .toList();
    }

    @Override
    @Transactional
    public boolean atualizarStatus(
            Long id,
            StatusExame statusAnterior,
            StatusExame novoStatus) {

        Objects.requireNonNull(statusAnterior);
        Objects.requireNonNull(novoStatus);

        return jpaRepository.atualizarStatus(
            id,
            statusAnterior.name(),
            novoStatus.name()
        ) == 1;
    }

    private SolicitacaoDeExame paraDominio(
            SolicitacaoDeExameEntity entity) {

        return new SolicitacaoDeExame(
            entity.getId(),
            entity.getAtendimentoId(),
            entity.getStatus()
        );
    }
}
