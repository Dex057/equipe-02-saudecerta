
package br.edu.ifpa.saudecerta.prontuario.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pro_prontuario")
public class ProntuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id",
            nullable = false,
            unique = true)
    private Long pacienteId;

    protected ProntuarioEntity() {
        // Construtor exigido pelo JPA
    }

    public ProntuarioEntity(Long pacienteId) {
        if (pacienteId == null || pacienteId <= 0) {
            throw new IllegalArgumentException(
                "Paciente inválido.");
        }

        this.pacienteId = pacienteId;
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }
}
