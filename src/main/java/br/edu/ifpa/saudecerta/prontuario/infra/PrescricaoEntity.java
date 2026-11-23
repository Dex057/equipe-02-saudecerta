
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pro_prescricao")
public class PrescricaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected PrescricaoEntity() {
        // Exigido pelo JPA
    }

    public PrescricaoEntity(
            Long atendimentoId,
            Instant dataHora) {

        if (atendimentoId == null || atendimentoId <= 0) {
            throw new IllegalArgumentException(
                "Atendimento invalido.");
        }

        this.atendimentoId = atendimentoId;
        this.dataHora = Objects.requireNonNull(
            dataHora, "Data e hora obrigatorias."
        );
    }

    public Long getId() {
        return id;
    }

    public Long getAtendimentoId() {
        return atendimentoId;
    }

    public Instant getDataHora() {
        return dataHora;
    }
}
