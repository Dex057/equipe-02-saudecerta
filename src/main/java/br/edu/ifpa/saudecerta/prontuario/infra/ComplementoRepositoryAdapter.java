
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpa.saudecerta.prontuario.domain.Complemento;
import br.edu.ifpa.saudecerta.prontuario.domain.ComplementoRepository;

@Repository
public class ComplementoRepositoryAdapter
        implements ComplementoRepository {

    private final ComplementoJpaRepository jpaRepository;

    public ComplementoRepositoryAdapter(
            ComplementoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Complemento inserir(Complemento complemento) {

        Objects.requireNonNull(
            complemento, "Complemento obrigatorio."
        );

        if (complemento.getId() != null) {
            throw new IllegalArgumentException(
                "Somente novos complementos podem ser inseridos."
            );
        }

        ComplementoEntity entity = new ComplementoEntity(
            complemento.getAtendimentoId(),
            complemento.getProfissionalId(),
            complemento.getTexto(),
            complemento.getDataHora()
        );

        ComplementoEntity salvo =
            jpaRepository.inserir(entity);

        return paraDominio(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Complemento> buscarPorId(Long id) {

        return jpaRepository.buscarPorId(id)
            .map(this::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complemento> buscarPorAtendimento(
            Long atendimentoId) {

        return jpaRepository
            .buscarPorAtendimento(atendimentoId)
            .stream()
            .map(this::paraDominio)
            .toList();
    }

    private Complemento paraDominio(
            ComplementoEntity entity) {

        return new Complemento(
            entity.getId(),
            entity.getAtendimentoId(),
            entity.getProfissionalId(),
            entity.getTexto(),
            entity.getDataHora()
        );
    }
}
