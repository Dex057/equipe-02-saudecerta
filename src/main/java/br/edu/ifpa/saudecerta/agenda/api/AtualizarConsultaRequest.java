package br.edu.ifpa.saudecerta.agenda.api;

import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public record AtualizarConsultaRequest(
        LocalDateTime dataHora,
        @Pattern(regexp = "CANCELADA", message = "status deve ser CANCELADA")
        String status) {
}
