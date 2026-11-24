package br.edu.ifpa.saudecerta.cadastro.domain;

import br.edu.ifpa.saudecerta.shared.paginacao.Pagina;
import java.util.Optional;

/** Porta de persistência de pacientes. A implementação fica em cadastro.infra. */
public interface PacienteRepository {

    /** Grava. Se a restrição de CPF ativo do banco for violada, lança CpfDuplicadoException. */
    Paciente salvar(Paciente paciente);

    Optional<Paciente> buscarPorId(Long id);

    Optional<Paciente> buscarAtivoPorCpf(Cpf cpf);

    /** Busca por prefixo do nome, sem diferenciar maiúsculas e acentos. Prefixo vazio lista todos. */
    Pagina<Paciente> buscarPorNome(String prefixo, int pagina, int tamanho);
}
