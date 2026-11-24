package br.edu.ifpa.saudecerta.cadastro.api;

import br.edu.ifpa.saudecerta.cadastro.application.PacienteDados;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Corpo de POST e PUT /api/pacientes. {@code ativo} é opcional e serve para inativar ou reativar no PUT. */
public record PacienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}",
                message = "deve ter 11 dígitos, com ou sem pontuação") String cpf,
        @NotNull @Past LocalDate dataNascimento,
        @Size(max = 20) String telefone,
        @Email @Size(max = 120) String email,
        Boolean ativo) {

    PacienteDados paraDados() {
        return new PacienteDados(nome, cpf, dataNascimento, telefone, email, ativo);
    }
}
