
package br.edu.ifpa.saudecerta.prontuario.api;

import br.edu.ifpa.saudecerta.prontuario.application.AutorizacaoProntuario;
import br.edu.ifpa.saudecerta.prontuario.application.ConsultarHistoricoService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes")
@ConditionalOnProperty(
    prefix = "saudecerta.prontuario",
    name = "api-habilitada",
    havingValue = "true"
)
public class ProntuarioHistoricoController {

    private final ConsultarHistoricoService service;
    private final AutorizacaoProntuario autorizacao;

    public ProntuarioHistoricoController(
            ConsultarHistoricoService service,
            AutorizacaoProntuario autorizacao) {

        this.service = service;
        this.autorizacao = autorizacao;
    }

    @GetMapping("/{id}/prontuario")
    public ResponseEntity<
            ConsultarHistoricoService.HistoricoResumo> consultar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page) {

        Long usuarioId = autorizacao
            .exigirUsuarioAutorizadoParaConsultarHistorico(id);

        var historico = service.consultarResumo(
            id,
            usuarioId,
            page
        );

        return ResponseEntity.ok(historico);
    }
}
