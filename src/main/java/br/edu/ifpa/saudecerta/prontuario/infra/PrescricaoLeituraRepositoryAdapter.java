package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.List;

import br.edu.ifpa.saudecerta.prontuario.domain.ItemDePrescricao;
import br.edu.ifpa.saudecerta.prontuario.domain.Prescricao;
import br.edu.ifpa.saudecerta.prontuario.domain.PrescricaoLeituraRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PrescricaoLeituraRepositoryAdapter implements PrescricaoLeituraRepository {

    private final PrescricaoJpaRepository prescricoes;
    private final ItemDePrescricaoJpaRepository itens;

    public PrescricaoLeituraRepositoryAdapter(
            PrescricaoJpaRepository prescricoes,
            ItemDePrescricaoJpaRepository itens) {
        this.prescricoes = prescricoes;
        this.itens = itens;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prescricao> buscarPorAtendimento(Long atendimentoId) {
        return prescricoes.buscarPorAtendimento(atendimentoId).stream()
            .map(p -> {
                List<ItemDePrescricao> medicamentos = itens.buscarPorPrescricao(p.getId())
                    .stream()
                    .map(i -> new ItemDePrescricao(
                        i.getId(), i.getMedicamento(), i.getDosagem(),
                        i.getPosologia(), i.getDuracao()))
                    .toList();
                return new Prescricao(
                    p.getId(), p.getAtendimentoId(), medicamentos, p.getDataHora());
            })
            .toList();
    }
}
