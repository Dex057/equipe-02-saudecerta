
package br.edu.ifpa.saudecerta.prontuario.domain;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.util.Objects;

public class SolicitacaoDeExame {

    private final Long id;
    private final Long atendimentoId;
    private StatusExame status;

    public SolicitacaoDeExame(Long id, Long atendimentoId) {
        this(id, atendimentoId, StatusExame.SOLICITADO);
    }

    public SolicitacaoDeExame(
            Long id,
            Long atendimentoId,
            StatusExame status) {

        if (atendimentoId == null || atendimentoId <= 0) {
            throw new IllegalArgumentException(
                    "O exame precisa estar vinculado a um atendimento.");
        }

        this.id = id;
        this.atendimentoId = atendimentoId;
        this.status = Objects.requireNonNull(status);
    }

    public void marcarRealizado() {
        validarTransicao();
        this.status = StatusExame.REALIZADO;
    }

    public void cancelar() {
        validarTransicao();
        this.status = StatusExame.CANCELADO;
    }

    private void validarTransicao() {
        if (status != StatusExame.SOLICITADO) {
            throw new RegraDeNegocioException(
                    "Não é possível alterar um exame finalizado.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getAtendimentoId() {
        return atendimentoId;
    }

    public StatusExame getStatus() {
        return status;
    }
}
