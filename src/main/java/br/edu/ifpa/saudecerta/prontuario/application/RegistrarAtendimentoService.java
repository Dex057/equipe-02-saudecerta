
package br.edu.ifpa.saudecerta.prontuario.application;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpa.saudecerta.agenda.AgendaApi;
import br.edu.ifpa.saudecerta.agenda.ConsultaResumo;
import br.edu.ifpa.saudecerta.prontuario.domain.Atendimento;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.ProntuarioRepository;
import br.edu.ifpa.saudecerta.shared.eventos.AtendimentoRegistrado;

public class RegistrarAtendimentoService {

    private final AgendaApi agendaApi;
    private final ProntuarioRepository prontuarioRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final ApplicationEventPublisher eventos;

    public RegistrarAtendimentoService(
            AgendaApi agendaApi,
            ProntuarioRepository prontuarioRepository,
            AtendimentoRepository atendimentoRepository,
            ApplicationEventPublisher eventos) {

        this.agendaApi = Objects.requireNonNull(agendaApi);
        this.prontuarioRepository =
            Objects.requireNonNull(prontuarioRepository);
        this.atendimentoRepository =
            Objects.requireNonNull(atendimentoRepository);
        this.eventos = Objects.requireNonNull(eventos);
    }

    @Transactional
    public Atendimento registrar(
            Long consultaId,
            Long usuarioId,
            String queixa,
            String evolucao,
            String conduta) {

        if (consultaId == null || consultaId <= 0) {
            throw new IllegalArgumentException(
                "Consulta invalida."
            );
        }

        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException(
                "Usuario invalido."
            );
        }

        ConsultaResumo consulta = agendaApi
            .buscarConsulta(consultaId)
            .orElseThrow(() -> new NoSuchElementException(
                "Consulta nao encontrada."
            ));

        if (consulta.pacienteId() == null) {
            throw new IllegalStateException(
                "Consulta sem paciente."
            );
        }

        agendaApi.marcarRealizada(consultaId);

        Long prontuarioId = prontuarioRepository
            .buscarOuCriarPorPaciente(consulta.pacienteId());

        Atendimento atendimento = new Atendimento(
            null,
            prontuarioId,
            consultaId,
            queixa,
            evolucao,
            conduta,
            Instant.now()
        );

        Atendimento salvo =
            atendimentoRepository.inserir(atendimento);

        eventos.publishEvent(
            new AtendimentoRegistrado(
                salvo.getId(),
                consulta.pacienteId(),
                usuarioId,
                "ATENDIMENTO",
                salvo.getDataHora()
            )
        );

        return salvo;
    }
}

