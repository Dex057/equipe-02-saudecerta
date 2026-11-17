package br.edu.ifpa.saudecerta.cadastro.application;

import java.util.List;

/** Entrada dos casos de uso de profissional. {@code ativo} nulo mantém o estado atual. */
public record ProfissionalDados(String nome, List<Long> especialidadeIds, Boolean ativo) {
}
