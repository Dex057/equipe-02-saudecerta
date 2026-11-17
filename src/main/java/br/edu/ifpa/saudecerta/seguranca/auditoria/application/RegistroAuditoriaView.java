package br.edu.ifpa.saudecerta.seguranca.auditoria.application;

import java.time.Instant;

public record RegistroAuditoriaView(Long id, String tipo, Long pacienteId, Long usuarioId, String acao,
                                    String detalhe, Instant dataHora) {
}
