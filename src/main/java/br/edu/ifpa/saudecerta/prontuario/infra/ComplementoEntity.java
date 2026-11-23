
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
@Table(name = "pro_complemento")
public class ComplementoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "profissional_id", nullable = false)
    private Long profissionalId;

    @Column(name = "texto", nullable = false, length = 4000)
    private String texto;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected ComplementoEntity() {
        // Exigido pelo JPA
    }

    public ComplementoEntity(
            Long atendimentoId,
            Long profissionalId,
            String texto,
            Instant dataHora) {

        if (atendimentoId == null || atendimentoId <= 0) {
            throw new IllegalArgumentException(
                "Atendimento invalido.");
        }

        if (profissionalId == null || profissionalId <= 0) {
            throw new IllegalArgumentException(
                "Profissional invalido.");
        }

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                "Complemento nao pode estar vazio.");
        }

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
