package br.edu.ifpa.saudecerta.cadastro;

import java.util.Optional;

/**
 * Interface pública do módulo Cadastro. Outros módulos só usam este tipo e os *Resumo
 * (arquitetura.md, seção 3.1, regra 3). O Cadastro implementa; Agenda e Prontuário consomem.
 */
public interface CadastroApi {

    Optional<PacienteResumo> buscarPaciente(Long id);

    Optional<ProfissionalResumo> buscarProfissional(Long id);
}
