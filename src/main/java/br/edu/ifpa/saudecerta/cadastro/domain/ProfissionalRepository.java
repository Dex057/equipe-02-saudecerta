package br.edu.ifpa.saudecerta.cadastro.domain;

import java.util.List;
import java.util.Optional;

public interface ProfissionalRepository {

    Profissional salvar(Profissional profissional);

    Optional<Profissional> buscarPorId(Long id);

    /** Lista por nome; com especialidadeId, só os que a têm. */
    List<Profissional> listar(Long especialidadeId);
}
