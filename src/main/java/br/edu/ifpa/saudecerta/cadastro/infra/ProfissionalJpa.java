package br.edu.ifpa.saudecerta.cadastro.infra;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cad_profissional")
class ProfissionalJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String nome;

    @Column(nullable = false)
    boolean ativo;

    /** Tabela de ligação: o banco só concede SELECT, INSERT e DELETE nela (ADR-006). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cad_profissional_especialidade", joinColumns = @JoinColumn(name = "profissional_id"))
    @Column(name = "especialidade_id", nullable = false)
    Set<Long> especialidadeIds = new HashSet<>();
}
