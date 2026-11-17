package br.edu.ifpa.saudecerta.cadastro.application;

import java.time.LocalDate;

/** Entrada dos casos de uso de paciente. {@code ativo} nulo mantém o estado atual. */
public record PacienteDados(String nome, String cpf, LocalDate dataNascimento, String telefone, String email,
                            Boolean ativo) {
}
