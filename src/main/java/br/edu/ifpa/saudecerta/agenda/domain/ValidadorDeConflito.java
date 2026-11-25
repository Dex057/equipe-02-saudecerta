package br.edu.ifpa.saudecerta.agenda.domain;

import java.time.LocalDateTime;

public class ValidadorDeConflito {

    private final ConsultaRepository consultas;

    public ValidadorDeConflito(ConsultaRepository consultas) {
        this.consultas = consultas;
    }

    public void validarConflito(Long profissionalId, LocalDateTime dataHora, Long ignorarConsultaId) {
        if (consultas.existeConflito(profissionalId, dataHora, ignorarConsultaId)) {
            throw new ConflitoDeHorarioException("O profissional já possui consulta agendada nesse horário.");
        }
    }
}
