
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpa.saudecerta.prontuario.domain.Atendimento;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;

@Repository
public class AtendimentoRepositoryAdapter
        implements AtendimentoRepository {

    private final AtendimentoJpaRepository jpaRepository;

    public AtendimentoRepositoryAdapter(
            AtendimentoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Atendimento inserir(Atendimento atendimento) {

        Objects.requireNonNull(
            atendimento, "Atendimento obrigatorio."
        );

        if (atendimento.getId() != null) {
            throw new IllegalArgumentException(
                "Somente novos atendimentos podem ser inseridos."
            );
        }

        AtendimentoEntity entity = new AtendimentoEntity(
            atendimento.getProntuarioId(),
            atendimento.getConsultaId(),
            atendimento.getQueixa(),
            atendimento.getEvolucao(),
            atendimento.getConduta(),
            atendimento.getDataHora()
        );

        return paraDominio(jpaRepository.inserir(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Atendimento> buscarPorId(Long id) {

        return jpaRepository.buscarPorId(id)
            .map(this::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Atendimento> buscarPaginaPorProntuario(
            Long prontuarioId,
            int pagina,
            int tamanho) {

        return jpaRepository
            .buscarPaginaPorProntuario(
                prontuarioId, pagina, tamanho
            )
            .stream()
            .map(this::paraDominio)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorProntuario(Long prontuarioId) {

        return jpaRepository.contarPorProntuario(
            prontuarioId
        );
    }

    private Atendimento paraDominio(
            AtendimentoEntity entity) {

        return new Atendimento(
            entity.getId(),
            entity.getProntuarioId(),
            entity.getConsultaId(),
            entity.getQueixa(),
            entity.getEvolucao(),
            entity.getConduta(),
            entity.getDataHora()
        );
    }
}
