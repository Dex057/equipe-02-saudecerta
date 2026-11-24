package br.edu.ifpa.saudecerta.seguranca.auditoria.application;

import br.edu.ifpa.saudecerta.seguranca.auditoria.domain.AuditoriaRepository;
import br.edu.ifpa.saudecerta.seguranca.auditoria.domain.FiltroAuditoria;
import br.edu.ifpa.saudecerta.seguranca.auditoria.domain.RegistroAuditoria;
import br.edu.ifpa.saudecerta.seguranca.auditoria.domain.TipoAuditoria;
import br.edu.ifpa.saudecerta.shared.paginacao.Pagina;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private static final int TAMANHO_MAXIMO = 100;

    private final AuditoriaRepository registros;

    public AuditoriaService(AuditoriaRepository registros) {
        this.registros = registros;
    }

    /**
     * Grava um registro. Sem {@code propagation} explícita: se quem publicou o evento já está numa transação, o
     * registro entra nela (e some junto se ela for desfeita); sem transação, abre a própria.
     */
    @Transactional
    public void registrar(TipoAuditoria tipo, Long pacienteId, Long usuarioId, String acao, String detalhe,
                          Instant dataHora) {
        registros.salvar(RegistroAuditoria.novo(tipo, pacienteId, usuarioId, acao, detalhe, dataHora));
    }

    @Transactional(readOnly = true)
    public Pagina<RegistroAuditoriaView> consultar(Long pacienteId, Long usuarioId, Instant de, Instant ate,
                                                   int pagina, int tamanho) {
        int tamanhoSeguro = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        return registros.consultar(new FiltroAuditoria(pacienteId, usuarioId, de, ate), Math.max(pagina, 0),
                tamanhoSeguro).mapear(AuditoriaService::view);
    }

    private static RegistroAuditoriaView view(RegistroAuditoria r) {
        return new RegistroAuditoriaView(r.id(), r.tipo().name(), r.pacienteId(), r.usuarioId(), r.acao(),
                r.detalhe(), r.dataHora());
    }
}
