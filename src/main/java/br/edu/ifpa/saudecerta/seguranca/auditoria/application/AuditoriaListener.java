package br.edu.ifpa.saudecerta.seguranca.auditoria.application;

import br.edu.ifpa.saudecerta.seguranca.auditoria.domain.TipoAuditoria;
import br.edu.ifpa.saudecerta.shared.eventos.AcessoNegado;
import br.edu.ifpa.saudecerta.shared.eventos.AtendimentoRegistrado;
import br.edu.ifpa.saudecerta.shared.eventos.ProntuarioAcessado;
import br.edu.ifpa.saudecerta.shared.eventos.StatusExameAlterado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Único consumidor dos eventos de auditoria (ADR-002). Nenhum módulo de negócio chama a Auditoria: eles só
 * publicam eventos de {@code shared.eventos}.
 * <ul>
 *   <li>Leitura: assíncrona, com 3 tentativas e, se persistir a falha, log {@code AUDITORIA_FALHA}.</li>
 *   <li>Escrita: síncrona, na mesma transação de quem publicou. Se a gravação falhar, a exceção desfaz também a
 *       alteração clínica: não existe alteração sem rastro.</li>
 *   <li>Acesso negado: síncrono, antes de o 403 sair. Se a gravação falhar, o acesso continua negado e a falha
 *       vai para o log {@code AUDITORIA_FALHA}.</li>
 * </ul>
 */
@Component
public class AuditoriaListener {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaListener.class);
    static final int TENTATIVAS_NA_LEITURA = 3;

    private final AuditoriaService auditoria;

    public AuditoriaListener(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @EventListener
    public void aoRegistrarAtendimento(AtendimentoRegistrado e) {
        auditoria.registrar(TipoAuditoria.ESCRITA, e.pacienteId(), e.usuarioId(), "ATENDIMENTO_REGISTRADO",
                "atendimentoId=" + e.atendimentoId() + "; tipo=" + e.tipo(), e.dataHora());
    }

    @EventListener
    public void aoAlterarStatusExame(StatusExameAlterado e) {
        auditoria.registrar(TipoAuditoria.ESCRITA, null, e.usuarioId(), "STATUS_EXAME_ALTERADO",
                "solicitacaoExameId=" + e.solicitacaoExameId() + "; atendimentoId=" + e.atendimentoId()
                        + "; " + e.statusAnterior() + " -> " + e.statusNovo(), e.dataHora());
    }

    @EventListener
    public void aoNegarAcesso(AcessoNegado e) {
        try {
            auditoria.registrar(TipoAuditoria.ACESSO_NEGADO, null, e.usuarioId(), "ACESSO_NEGADO",
                    "recurso=" + e.recurso() + "; motivo=" + e.motivo(), e.dataHora());
        } catch (RuntimeException falha) {
            log.error("AUDITORIA_FALHA tipo=ACESSO_NEGADO usuarioId={} recurso={}", e.usuarioId(), e.recurso(), falha);
        }
    }

    @Async("auditoriaExecutor")
    @EventListener
    public void aoAcessarProntuario(ProntuarioAcessado e) {
        RuntimeException ultima = null;
        for (int tentativa = 1; tentativa <= TENTATIVAS_NA_LEITURA; tentativa++) {
            try {
                auditoria.registrar(TipoAuditoria.LEITURA, e.pacienteId(), e.usuarioId(), "PRONTUARIO_ACESSADO",
                        null, e.dataHora());
                return;
            } catch (RuntimeException falha) {
                ultima = falha;
            }
        }
        log.error("AUDITORIA_FALHA tipo=LEITURA pacienteId={} usuarioId={} tentativas={}", e.pacienteId(),
                e.usuarioId(), TENTATIVAS_NA_LEITURA, ultima);
    }
}
