
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
@Table(name = "pro_atendimento")
public class AtendimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prontuario_id", nullable = false)
    private Long prontuarioId;

    @Column(name = "consulta_id", nullable = false, unique = true)
    private Long consultaId;

    @Column(name = "queixa", nullable = false, length = 4000)
    private String queixa;

    @Column(name = "evolucao", nullable = false, length = 4000)
    private String evolucao;

    @Column(name = "conduta", nullable = false, length = 4000)
    private String conduta;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected AtendimentoEntity() {
        // Exigido pelo JPA
    }

    public AtendimentoEntity(
            Long prontuarioId,
            Long consultaId,
            String queixa,
            String evolucao,
            String conduta,
            Instant dataHora) {

        if (prontuarioId == null || prontuarioId <= 0) {
            throw new IllegalArgumentException(
                "Prontuario invalido.");
        }

        if (consultaId == null || consultaId <= 0) {
            throw new IllegalArgumentException(
                "Consulta invalida.");
        }

        this.prontuarioId = prontuarioId;
        this.consultaId = consultaId;
        this.queixa = validarTexto(queixa);
        this.evolucao = validarTexto(evolucao);
        this.conduta = validarTexto(conduta);
        this.dataHora = Objects.requireNonNull(dataHora);
    }

    private static String validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                "O campo clinico nao pode estar vazio.");
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
