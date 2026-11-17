package br.edu.ifpa.saudecerta.seguranca.auditoria.domain;

import java.time.Instant;

/** Linha da trilha de auditoria. Imutável: a tabela só aceita INSERT e SELECT (ADR-006). */
public record RegistroAuditoria(Long id, TipoAuditoria tipo, Long pacienteId, Long usuarioId, String acao,
                                String detalhe, Instant dataHora) {

    public static RegistroAuditoria novo(TipoAuditoria tipo, Long pacienteId, Long usuarioId, String acao,
                                         String detalhe, Instant dataHora) {
        return new RegistroAuditoria(null, tipo, pacienteId, usuarioId, acao, detalhe, dataHora);
    }
}
