package br.edu.ifpa.saudecerta.shared.eventos;

import java.time.Instant;

/** Acesso recusado pela Segurança. Gravado de forma síncrona antes de o 403 sair (ADR-002). */
public record AcessoNegado(Long usuarioId, String recurso, String motivo, Instant dataHora) {
}
