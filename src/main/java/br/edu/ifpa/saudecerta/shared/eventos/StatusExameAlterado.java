package br.edu.ifpa.saudecerta.shared.eventos;

import java.time.Instant;

/** Mudança de status de uma solicitação de exame (RF09). Síncrono, na mesma transação (ADR-002, ADR-003). */
public record StatusExameAlterado(Long solicitacaoExameId, Long atendimentoId, Long usuarioId,
                                  String statusAnterior, String statusNovo, Instant dataHora) {
}
