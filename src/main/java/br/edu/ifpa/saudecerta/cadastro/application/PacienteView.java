package br.edu.ifpa.saudecerta.cadastro.application;

import java.time.LocalDate;

public record PacienteView(Long id, String nome, String cpf, LocalDate dataNascimento, String telefone,
                           String email, boolean ativo) {
}
