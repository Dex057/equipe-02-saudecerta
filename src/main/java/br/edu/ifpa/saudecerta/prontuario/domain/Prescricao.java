
package br.edu.ifpa.saudecerta.prontuario.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Prescricao {

    private final Long id;
    private final Long atendimentoId;
    private final List<ItemDePrescricao> itens;
    private final Instant dataHora;

    public Prescricao(
            Long id,
            Long atendimentoId,
            List<ItemDePrescricao> itens,
            Instant dataHora) {

        if (atendimentoId == null || atendimentoId <= 0) {
            throw new IllegalArgumentException(
                "A prescrição precisa de um atendimento válido.");
        }

        Objects.requireNonNull(itens, "Itens obrigatórios.");

        if (itens.isEmpty()) {
            throw new IllegalArgumentException(
                "A prescrição precisa de pelo menos um item.");
        }

        this.id = id;
        this.atendimentoId = atendimentoId;
        this.itens = List.copyOf(itens);
        this.dataHora = Objects.requireNonNull(
                dataHora, "Data e hora obrigatórias.");
    }

    public Long getId() {
        return id;
    }

    public Long getAtendimentoId() {
        return atendimentoId;
    }

    public List<ItemDePrescricao> getItens() {
        return itens;
    }

    public Instant getDataHora() {
        return dataHora;
    }
}
