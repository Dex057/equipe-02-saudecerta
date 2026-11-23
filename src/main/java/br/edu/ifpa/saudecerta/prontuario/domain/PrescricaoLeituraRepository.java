package br.edu.ifpa.saudecerta.prontuario.domain;

import java.util.List;

/** Porta de leitura de prescricoes e itens para composicao do historico clinico. */
public interface PrescricaoLeituraRepository {
    List<Prescricao> buscarPorAtendimento(Long atendimentoId);
}
