package br.edu.ifpa.saudecerta.seguranca.auditoria.domain;

import java.time.Instant;

/** Filtros da consulta da trilha; qualquer campo nulo é ignorado. */
public record FiltroAuditoria(Long pacienteId, Long usuarioId, Instant de, Instant ate) {
}
