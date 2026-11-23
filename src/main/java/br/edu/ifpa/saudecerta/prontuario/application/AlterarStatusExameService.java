
package br.edu.ifpa.saudecerta.prontuario.application;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpa.saudecerta.prontuario.domain.SolicitacaoDeExame;
import br.edu.ifpa.saudecerta.prontuario.domain.SolicitacaoDeExameRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.StatusExame;
import br.edu.ifpa.saudecerta.shared.eventos.StatusExameAlterado;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;

@Service
public class AlterarStatusExameService {

    private final SolicitacaoDeExameRepository exameRepository;
    private final ApplicationEventPublisher eventos;

    public AlterarStatusExameService(
            SolicitacaoDeExameRepository exameRepository,
            ApplicationEventPublisher eventos) {

        this.exameRepository =
            Objects.requireNonNull(exameRepository);
        this.eventos = Objects.requireNonNull(eventos);
    }

    @Transactional
    public SolicitacaoDeExame alterar(
            Long exameId,
            Long usuarioId,
            StatusExame novoStatus) {

        if (exameId == null || exameId <= 0) {
            throw new IllegalArgumentException(
                "Exame invalido."
            );
        }

        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException(
                "Usuario invalido."
            );
        }

        Objects.requireNonNull(
            novoStatus, "Novo status obrigatorio."
        );

        SolicitacaoDeExame exame = exameRepository
            .buscarPorId(exameId)
            .orElseThrow(() -> new NoSuchElementException(
                "Exame nao encontrado."
            ));

        StatusExame statusAnterior = exame.getStatus();

        switch (novoStatus) {
            case REALIZADO -> exame.marcarRealizado();
            case CANCELADO -> exame.cancelar();
            case SOLICITADO -> throw new RegraDeNegocioException(
                "Transicao de status invalida."
            );
        }

        boolean atualizado = exameRepository.atualizarStatus(
            exameId,
            statusAnterior,
            novoStatus
        );

        if (!atualizado) {
            throw new RegraDeNegocioException(
                "O status do exame foi alterado por outra operacao."
            );
        }

        eventos.publishEvent(
            new StatusExameAlterado(
                exameId,
                exame.getAtendimentoId(),
                usuarioId,
                statusAnterior.name(),
                novoStatus.name(),
                Instant.now()
            )
        );

        return exame;
    }

@Transactional
public void alterarPorTexto(
        Long exameId,
        Long usuarioId,
        String status) {

    StatusExame novoStatus =
        StatusExameParser.converter(status);

    alterar(exameId, usuarioId, novoStatus);
}

}
