
package br.edu.ifpa.saudecerta.prontuario.domain;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoDeExameRepository {

    SolicitacaoDeExame inserir(
            SolicitacaoDeExame exame);

    Optional<SolicitacaoDeExame> buscarPorId(
            Long id);

    List<SolicitacaoDeExame> buscarPorAtendimento(
            Long atendimentoId);

    boolean atualizarStatus(
            Long id,
            StatusExame statusAnterior,
            StatusExame novoStatus);
}
