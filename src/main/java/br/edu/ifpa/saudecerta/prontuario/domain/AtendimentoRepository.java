
package br.edu.ifpa.saudecerta.prontuario.domain;

import java.util.List;
import java.util.Optional;

public interface AtendimentoRepository {

    Atendimento inserir(Atendimento atendimento);

    Optional<Atendimento> buscarPorId(Long id);

    List<Atendimento> buscarPaginaPorProntuario(
            Long prontuarioId,
            int pagina,
            int tamanho);

    long contarPorProntuario(Long prontuarioId);
}
