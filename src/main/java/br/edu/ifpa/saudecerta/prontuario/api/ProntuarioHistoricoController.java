
package br.edu.ifpa.saudecerta.prontuario.api;

import br.edu.ifpa.saudecerta.prontuario.application.AutorizacaoProntuario;
import br.edu.ifpa.saudecerta.prontuario.application.ConsultarHistoricoCompletoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes")
public class ProntuarioHistoricoController {

    private final ConsultarHistoricoCompletoService service;
    private final AutorizacaoProntuario autorizacao;

    public ProntuarioHistoricoController(
            ConsultarHistoricoCompletoService service,
            AutorizacaoProntuario autorizacao) {

        this.service = service;
        this.autorizacao = autorizacao;
    }

    @GetMapping("/{id}/prontuario")
    public ResponseEntity<
            ConsultarHistoricoCompletoService.HistoricoResumo> consultar(
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
