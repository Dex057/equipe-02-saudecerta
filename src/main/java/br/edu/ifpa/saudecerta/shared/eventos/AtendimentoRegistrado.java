package br.edu.ifpa.saudecerta.shared.eventos;

import java.time.Instant;

/**
 * Escrita no prontuário (atendimento, prescrição, exame ou complemento).
 * Tratado de forma síncrona, na mesma transação de quem publicou (ADR-002).
 */
public record AtendimentoRegistrado(Long atendimentoId, Long pacienteId, Long usuarioId, String tipo,
                                    Instant dataHora) {
}
