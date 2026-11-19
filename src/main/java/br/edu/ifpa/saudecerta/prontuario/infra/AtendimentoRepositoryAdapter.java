
package br.edu.ifpa.saudecerta.prontuario.infra;

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

        AtendimentoEntity salvo =
            jpaRepository.inserir(entity);

        return paraDominio(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Atendimento> buscarPorId(Long id) {

        return jpaRepository.buscarPorId(id)
            .map(this::paraDominio);
    }

    private Atendimento paraDominio(AtendimentoEntity entity) {

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
