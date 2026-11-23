
package br.edu.ifpa.saudecerta.prontuario.domain;

import java.time.Instant;
import java.util.Objects;

public final class Complemento {

    private final Long id;
    private final Long atendimentoId;
    private final Long profissionalId;
    private final String texto;
    private final Instant dataHora;

    public Complemento(
            Long id,
            Long atendimentoId,
            Long profissionalId,
            String texto,
            Instant dataHora) {

        if (atendimentoId == null || atendimentoId <= 0) {
            throw new IllegalArgumentException(
                    "Atendimento inválido.");
        }

        if (profissionalId == null || profissionalId <= 0) {
            throw new IllegalArgumentException(
                    "Profissional inválido.");
        }

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "O complemento não pode estar vazio.");
        }

        this.id = id;
        this.atendimentoId = atendimentoId;
        this.profissionalId = profissionalId;
        this.texto = texto.trim();
        this.dataHora = Objects.requireNonNull(dataHora);
    }

    public Long getId() {
        return id;
    }

    public Long getAtendimentoId() {
        return atendimentoId;
    }

    public Long getProfissionalId() {
        return profissionalId;
    }

    public String getTexto() {
        return texto;
    }

    public Instant getDataHora() {
        return dataHora;
    }
}
