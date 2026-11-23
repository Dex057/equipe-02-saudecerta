
package br.edu.ifpa.saudecerta.prontuario.domain;

import java.time.Instant;
import java.util.Objects;

public final class Atendimento {

    private final Long id;
    private final Long prontuarioId;
    private final Long consultaId;
    private final String queixa;
    private final String evolucao;
    private final String conduta;
    private final Instant dataHora;

    public Atendimento(
            Long id,
            Long prontuarioId,
            Long consultaId,
            String queixa,
            String evolucao,
            String conduta,
            Instant dataHora) {

        if (prontuarioId == null || prontuarioId <= 0) {
            throw new IllegalArgumentException("Prontuário inválido.");
        }

        if (consultaId == null || consultaId <= 0) {
            throw new IllegalArgumentException("Consulta inválida.");
        }

        this.id = id;
        this.prontuarioId = prontuarioId;
        this.consultaId = consultaId;
        this.queixa = validarTexto(queixa, "Queixa");
        this.evolucao = validarTexto(evolucao, "Evolução");
        this.conduta = validarTexto(conduta, "Conduta");
        this.dataHora = Objects.requireNonNull(dataHora, "Data e hora obrigatórias.");
    }

    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
        return texto.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getProntuarioId() {
        return prontuarioId;
    }

    public Long getConsultaId() {
        return consultaId;
    }

    public String getQueixa() {
        return queixa;
    }

    public String getEvolucao() {
        return evolucao;
    }

    public String getConduta() {
        return conduta;
    }

    public Instant getDataHora() {
        return dataHora;
    }
}
