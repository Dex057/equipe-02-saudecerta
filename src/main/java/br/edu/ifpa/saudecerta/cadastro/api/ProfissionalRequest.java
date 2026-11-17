package br.edu.ifpa.saudecerta.cadastro.api;

import br.edu.ifpa.saudecerta.cadastro.application.ProfissionalDados;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Corpo de POST e PUT /api/profissionais. RF02: ao menos uma especialidade. */
public record ProfissionalRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotEmpty List<@NotNull Long> especialidadeIds,
        Boolean ativo) {

    ProfissionalDados paraDados() {
        return new ProfissionalDados(nome, especialidadeIds, ativo);
    }
}
