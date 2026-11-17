package br.edu.ifpa.saudecerta.shared.eventos;

import java.time.Instant;

/** Leitura do prontuário. Tratado de forma assíncrona pela Auditoria (ADR-002). */
public record ProntuarioAcessado(Long pacienteId, Long usuarioId, Instant dataHora) {
}
