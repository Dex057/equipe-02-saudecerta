package br.edu.ifpa.saudecerta.prontuario.application;

import java.util.NoSuchElementException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.ifpa.saudecerta.agenda.AgendaApi;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.ProntuarioRepository;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;

/**
 * Entrada HTTP do caso de uso; a Agenda real pode entrar depois do esqueleto.
 * O usuarioId vem de um header apenas no prototipo, sem autenticacao real.
 */
@Service
public class RegistrarAtendimentoFacade {

    private final ObjectProvider<AgendaApi> agendaProvider;
    private final ProntuarioRepository prontuarios;
    private final AtendimentoRepository atendimentos;
    private final ApplicationEventPublisher eventos;

    public RegistrarAtendimentoFacade(ObjectProvider<AgendaApi> agendaProvider,
            ProntuarioRepository prontuarios, AtendimentoRepository atendimentos,
            ApplicationEventPublisher eventos) {
        this.agendaProvider = agendaProvider;
        this.prontuarios = prontuarios;
        this.atendimentos = atendimentos;
        this.eventos = eventos;
    }

    @Transactional
    public Long registrar(Long consultaId, Long usuarioId,
            String queixa, String evolucao, String conduta) {
        AgendaApi agenda = agendaProvider.getIfAvailable();
        if (agenda == null) {
            throw new IllegalStateException("AgendaApi ainda nao integrada.");
        }
        try {
            return new RegistrarAtendimentoService(agenda, prontuarios, atendimentos, eventos)
                    .registrar(consultaId, usuarioId, queixa, evolucao, conduta).getId();
        } catch (NoSuchElementException e) {
            throw new RecursoNaoEncontradoException(e.getMessage());
        }
    }
}