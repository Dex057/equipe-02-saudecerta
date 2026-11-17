package br.edu.ifpa.saudecerta.cadastro.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EspecialidadeRequest(@NotBlank @Size(max = 80) String nome) {
}
