package br.edu.ifpa.saudecerta.agenda.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record MarcarConsultaRequest(
        @NotNull @Positive Long pacienteId,
        @NotNull @Positive Long profissionalId,
        @NotNull LocalDateTime dataHora) {
}
