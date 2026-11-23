package br.edu.ifpa.saudecerta.agenda.domain;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Consulta {

    private final Long id;
    private final Long pacienteId;
    private final Long profissionalId;
    private LocalDateTime dataHora;
    private StatusConsulta status;

    public Consulta(Long id, Long pacienteId, Long profissionalId, LocalDateTime dataHora,
                    StatusConsulta status) {
        this.id = id;
        this.pacienteId = Objects.requireNonNull(pacienteId);
        this.profissionalId = Objects.requireNonNull(profissionalId);
        this.dataHora = Objects.requireNonNull(dataHora);
        this.status = Objects.requireNonNull(status);
    }

    public static Consulta agendada(Long pacienteId, Long profissionalId, LocalDateTime dataHora) {
        return new Consulta(null, pacienteId, profissionalId, dataHora, StatusConsulta.AGENDADA);
    }

    public void cancelar() {
        exigirAgendada("Somente consultas agendadas podem ser canceladas.");
        status = StatusConsulta.CANCELADA;
    }

    public void remarcar(LocalDateTime novoHorario) {
        exigirAgendada("Somente consultas agendadas podem ser remarcadas.");
        dataHora = Objects.requireNonNull(novoHorario);
    }

    public void marcarRealizada(LocalDate hoje) {
        exigirAgendada("Somente consultas agendadas podem ser marcadas como realizadas.");
        if (dataHora.toLocalDate().isAfter(hoje)) {
            throw new RegraDeNegocioException("Consultas futuras não podem ser marcadas como realizadas.");
        }
        status = StatusConsulta.REALIZADA;
    }

    private void exigirAgendada(String mensagem) {
        if (status != StatusConsulta.AGENDADA) {
            throw new RegraDeNegocioException(mensagem);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getProfissionalId() {
        return profissionalId;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public StatusConsulta getStatus() {
        return status;
    }
}
