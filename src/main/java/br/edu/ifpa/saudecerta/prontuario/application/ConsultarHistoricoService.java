
package br.edu.ifpa.saudecerta.prontuario.application;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpa.saudecerta.prontuario.domain.Atendimento;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.ProntuarioRepository;
import br.edu.ifpa.saudecerta.shared.eventos.ProntuarioAcessado;

@Service
public class ConsultarHistoricoService {

    private static final int TAMANHO_PAGINA = 10;

    private final ProntuarioRepository prontuarioRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final ApplicationEventPublisher eventos;

    public ConsultarHistoricoService(
            ProntuarioRepository prontuarioRepository,
            AtendimentoRepository atendimentoRepository,
            ApplicationEventPublisher eventos) {

        this.prontuarioRepository =
            Objects.requireNonNull(prontuarioRepository);
        this.atendimentoRepository =
            Objects.requireNonNull(atendimentoRepository);
        this.eventos = Objects.requireNonNull(eventos);
    }

    public record PaginaHistorico(
            Long pacienteId,
            int pagina,
            int tamanho,
            long total,
            List<Atendimento> atendimentos) {
    }

    @Transactional(readOnly = true)
    public PaginaHistorico consultar(
            Long pacienteId,
            Long usuarioId,
            int pagina) {

        if (pacienteId == null || pacienteId <= 0) {
            throw new IllegalArgumentException(
                "Paciente invalido."
            );
        }

        if (usuarioId != null && usuarioId <= 0) {
            throw new IllegalArgumentException(
                "Usuario invalido."
            );
        }

        if (pagina < 0) {
            throw new IllegalArgumentException(
                "Pagina invalida."
            );
        }

        Optional<Long> prontuarioId =
            prontuarioRepository.buscarIdPorPaciente(
                pacienteId
            );

        List<Atendimento> atendimentos = List.of();
        long total = 0;

        if (prontuarioId.isPresent()) {

            Long id = prontuarioId.orElseThrow();

            atendimentos =
                atendimentoRepository.buscarPaginaPorProntuario(
                    id,
                    pagina,
                    TAMANHO_PAGINA
                );

            total = atendimentoRepository
                .contarPorProntuario(id);
        }

        eventos.publishEvent(
            new ProntuarioAcessado(
                pacienteId,
                usuarioId,
                Instant.now()
            )
        );

        return new PaginaHistorico(
            pacienteId,
            pagina,
            TAMANHO_PAGINA,
            total,
            List.copyOf(atendimentos)
        );
    }

public record AtendimentoResumo(
        Long id,
        Long consultaId,
        String queixa,
        String evolucao,
        String conduta,
        Instant dataHora) {
}

public record HistoricoResumo(
        Long pacienteId,
        int pagina,
        int tamanho,
        long total,
        List<AtendimentoResumo> atendimentos) {
}

@Transactional(readOnly = true)
public HistoricoResumo consultarResumo(
        Long pacienteId,
        Long usuarioId,
        int pagina) {

    PaginaHistorico historico = consultar(
        pacienteId,
        usuarioId,
        pagina
    );

    List<AtendimentoResumo> atendimentos =
        historico.atendimentos()
            .stream()
            .map(a -> new AtendimentoResumo(
                a.getId(),
                a.getConsultaId(),
                a.getQueixa(),
                a.getEvolucao(),
                a.getConduta(),
                a.getDataHora()
            ))
            .toList();

    return new HistoricoResumo(
        historico.pacienteId(),
        historico.pagina(),
        historico.tamanho(),
        historico.total(),
        atendimentos
    );
}

}
