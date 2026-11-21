
package br.edu.ifpa.saudecerta.prontuario.domain;

import java.util.List;
import java.util.Optional;

public interface ComplementoRepository {

    Complemento inserir(Complemento complemento);

    Optional<Complemento> buscarPorId(Long id);

    List<Complemento> buscarPorAtendimento(
            Long atendimentoId);
}
