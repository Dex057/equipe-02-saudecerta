
package br.edu.ifpa.saudecerta.prontuario.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarAtendimentoRequest(

    @NotBlank
    @Size(max = 4000)
    String queixa,

    @NotBlank
    @Size(max = 4000)
    String evolucao,

    @NotBlank
    @Size(max = 4000)
    String conduta

) {}
