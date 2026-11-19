package br.edu.ifpa.saudecerta.prontuario.domain;

import java.util.Optional;

public interface AtendimentoRepository {

    Atendimento inserir(Atendimento atendimento);

    Optional<Atendimento> buscarPorId(Long id);
}