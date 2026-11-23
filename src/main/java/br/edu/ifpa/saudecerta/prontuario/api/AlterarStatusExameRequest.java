
package br.edu.ifpa.saudecerta.prontuario.api;

import jakarta.validation.constraints.NotBlank;

public record AlterarStatusExameRequest(

    @NotBlank
    String status

) {}
