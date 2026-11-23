package br.edu.ifpa.saudecerta.agenda.infra;

import br.edu.ifpa.saudecerta.agenda.domain.StatusConsulta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "age_consulta")
class ConsultaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "profissional_id", nullable = false)
    private Long profissionalId;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status;

    @Column(name = "horario_ocupado")
    private LocalDateTime horarioOcupado;

    protected ConsultaEntity() {
    }

    ConsultaEntity(Long pacienteId, Long profissionalId, LocalDateTime dataHora, StatusConsulta status) {
        this.pacienteId = pacienteId;
        this.profissionalId = profissionalId;
        atualizar(dataHora, status);
    }

    void atualizar(LocalDateTime dataHora, StatusConsulta status) {
        this.dataHora = dataHora;
        this.status = status;
        this.horarioOcupado = status == StatusConsulta.AGENDADA ? dataHora : null;
    }

    Long getId() {
        return id;
    }

    Long getPacienteId() {
        return pacienteId;
    }

    Long getProfissionalId() {
        return profissionalId;
    }

    LocalDateTime getDataHora() {
        return dataHora;
    }

    StatusConsulta getStatus() {
        return status;
    }

    LocalDateTime getHorarioOcupado() {
        return horarioOcupado;
    }
}
