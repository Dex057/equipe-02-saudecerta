package br.edu.ifpa.saudecerta.cadastro;

import java.util.List;

public record ProfissionalResumo(Long id, String nome, List<Long> especialidadeIds, boolean ativo) {
}
