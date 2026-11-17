package br.edu.ifpa.saudecerta.seguranca.auditoria.domain;

/** Os três caminhos do ADR-002: leitura (assíncrona), escrita e acesso negado (síncronos). */
public enum TipoAuditoria {
    LEITURA, ESCRITA, ACESSO_NEGADO
}
