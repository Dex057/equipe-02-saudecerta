package br.edu.ifpa.saudecerta.agenda;

import java.time.LocalDateTime;

public record ConsultaResumo(Long id, Long pacienteId, Long profissionalId, LocalDateTime dataHora,
                             String status) {
}
