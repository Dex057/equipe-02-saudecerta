
package br.edu.ifpa.saudecerta.prontuario.infra;

import br.edu.ifpa.saudecerta.prontuario.domain.StatusExame;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pro_solicitacao_exame")
public class SolicitacaoDeExameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "atendimento_id",
        nullable = false,
        updatable = false
    )
    private Long atendimentoId;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 16,
        updatable = false
    )
    private StatusExame status;

    protected SolicitacaoDeExameEntity() {
        // Exigido pelo JPA
    }

    public SolicitacaoDeExameEntity(Long atendimentoId) {

        if (atendimentoId == null || atendimentoId <= 0) {
            throw new IllegalArgumentException(
                "Atendimento invalido."
            );
        }

        this.atendimentoId = atendimentoId;
        this.status = StatusExame.SOLICITADO;
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
