package br.edu.ifpa.saudecerta.seguranca.auditoria.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "aud_registro_auditoria")
class RegistroAuditoriaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /** Nome do TipoAuditoria. Texto simples, para o esquema ser igual no H2 e no PostgreSQL. */
    @Column(nullable = false)
    String tipo;

    @Column(name = "paciente_id")
    Long pacienteId;

    @Column(name = "usuario_id")
    Long usuarioId;

    @Column(nullable = false)
    String acao;

    String detalhe;

    @Column(name = "data_hora", nullable = false)
    Instant dataHora;
}
