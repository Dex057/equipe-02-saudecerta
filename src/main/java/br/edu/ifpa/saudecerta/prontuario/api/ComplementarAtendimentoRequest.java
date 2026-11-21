
package br.edu.ifpa.saudecerta.prontuario.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplementarAtendimentoRequest(

    @NotBlank
    @Size(max = 4000)
    String texto

) {}
