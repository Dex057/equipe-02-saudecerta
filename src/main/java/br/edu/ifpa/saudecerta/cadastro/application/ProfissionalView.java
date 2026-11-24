package br.edu.ifpa.saudecerta.cadastro.application;

import java.util.List;

public record ProfissionalView(Long id, String nome, boolean ativo, List<EspecialidadeView> especialidades) {
}
