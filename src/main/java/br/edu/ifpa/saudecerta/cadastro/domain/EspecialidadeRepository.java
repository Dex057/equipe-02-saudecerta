package br.edu.ifpa.saudecerta.cadastro.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EspecialidadeRepository {

    /** Grava. Nome repetido lança ConflitoException. */
    Especialidade salvar(Especialidade especialidade);

    Optional<Especialidade> buscarPorId(Long id);

    Optional<Especialidade> buscarPorNome(String nome);

    List<Especialidade> buscarPorIds(Collection<Long> ids);

    List<Especialidade> listar();
}
