package br.edu.ifpa.saudecerta.cadastro.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "cad_paciente")
class PacienteJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String nome;

    @Column(name = "nome_normalizado", nullable = false)
    String nomeNormalizado;

    @Column(nullable = false)
    String cpf;

    /** Igual ao CPF enquanto o paciente está ativo; NULL quando inativo. UNIQUE no banco (RN01, ADR-006). */
    @Column(name = "cpf_ativo")
    String cpfAtivo;

    @Column(name = "data_nascimento", nullable = false)
    LocalDate dataNascimento;

    String telefone;

    String email;

    @Column(nullable = false)
    boolean ativo;
}
