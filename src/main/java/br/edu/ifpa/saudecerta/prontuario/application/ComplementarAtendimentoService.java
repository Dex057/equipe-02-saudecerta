
package br.edu.ifpa.saudecerta.prontuario.application;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpa.saudecerta.prontuario.domain.Atendimento;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.Complemento;
import br.edu.ifpa.saudecerta.prontuario.domain.ComplementoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.ProntuarioRepository;
import br.edu.ifpa.saudecerta.shared.eventos.AtendimentoRegistrado;

@Service
public class ComplementarAtendimentoService {

    private final AtendimentoRepository atendimentoRepository;
    private final ComplementoRepository complementoRepository;
    private final ProntuarioRepository prontuarioRepository;
    private final ApplicationEventPublisher eventos;

    public ComplementarAtendimentoService(
            AtendimentoRepository atendimentoRepository,
            ComplementoRepository complementoRepository,
            ProntuarioRepository prontuarioRepository,
            ApplicationEventPublisher eventos) {

        this.atendimentoRepository =
            Objects.requireNonNull(atendimentoRepository);
        this.complementoRepository =
            Objects.requireNonNull(complementoRepository);
        this.prontuarioRepository =
            Objects.requireNonNull(prontuarioRepository);
        this.eventos = Objects.requireNonNull(eventos);
    }

    @Transactional
    public Complemento complementar(
            Long atendimentoId,
            Long profissionalId,
            Long usuarioId,
            String texto) {

        if (atendimentoId == null || atendimentoId <= 0) {
            throw new IllegalArgumentException(
                "Atendimento invalido."
            );
        }

        if (profissionalId == null || profissionalId <= 0) {
            throw new IllegalArgumentException(
                "Profissional invalido."
            );
        }

        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException(
                "Usuario invalido."
            );
        }

        Atendimento atendimento = atendimentoRepository
            .buscarPorId(atendimentoId)
            .orElseThrow(() -> new NoSuchElementException(
                "Atendimento nao encontrado."
            ));

        Long pacienteId = prontuarioRepository
            .buscarPacienteIdPorId(atendimento.getProntuarioId())
            .orElseThrow(() -> new NoSuchElementException(
                "Prontuario nao encontrado."
            ));

        Complemento complemento = new Complemento(
            null,
            atendimentoId,
            profissionalId,
            texto,
            Instant.now()
        );

        Complemento salvo =
            complementoRepository.inserir(complemento);

        eventos.publishEvent(
            new AtendimentoRegistrado(
                atendimentoId,
                pacienteId,
                usuarioId,
                "COMPLEMENTO",
                salvo.getDataHora()
            )
        );

        return salvo;
    }

@Transactional
public Long complementarRetornandoId(
        Long atendimentoId,
        Long profissionalId,
        Long usuarioId,
        String texto) {

    return complementar(
        atendimentoId,
        profissionalId,
        usuarioId,
        texto
    ).getId();
}

}
