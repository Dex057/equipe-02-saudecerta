package br.edu.ifpa.saudecerta.prontuario.application;

import java.util.NoSuchElementException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpa.saudecerta.agenda.AgendaApi;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.ProntuarioRepository;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;

/**
 * Adaptador de caso de uso para HTTP.
 * Nao e criado sem a flag explicita, pois depende de Agenda e autorizacao reais.
 */
@Service
@ConditionalOnProperty(prefix = "saudecerta.prontuario",
        name = "api-habilitada", havingValue = "true")
public class RegistrarAtendimentoFacade {

    private final RegistrarAtendimentoService registrar;

    public RegistrarAtendimentoFacade(
            AgendaApi agendaApi,
            ProntuarioRepository prontuarioRepository,
            AtendimentoRepository atendimentoRepository,
            ApplicationEventPublisher eventos) {
        this.registrar = new RegistrarAtendimentoService(
                agendaApi, prontuarioRepository, atendimentoRepository, eventos);
    }

    @Transactional
    public Long registrar(Long consultaId, Long usuarioId,
                          String queixa, String evolucao, String conduta) {
        try {
            // Uma transacao envolve a Agenda e a insercao do atendimento.
            return registrar.registrar(
                    consultaId, usuarioId, queixa, evolucao, conduta).getId();
        } catch (NoSuchElementException e) {
            throw new RecursoNaoEncontradoException(e.getMessage());
        }
    }
}
