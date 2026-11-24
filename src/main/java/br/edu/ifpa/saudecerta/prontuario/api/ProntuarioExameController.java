
package br.edu.ifpa.saudecerta.prontuario.api;

import br.edu.ifpa.saudecerta.prontuario.application.AlterarStatusExameService;
import br.edu.ifpa.saudecerta.prontuario.application.AutorizacaoProntuario;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exames")
public class ProntuarioExameController {

    private final AlterarStatusExameService service;
    private final AutorizacaoProntuario autorizacao;

    public ProntuarioExameController(
            AlterarStatusExameService service,
            AutorizacaoProntuario autorizacao) {

        this.service = service;
        this.autorizacao = autorizacao;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody AlterarStatusExameRequest request) {

        Long usuarioId = autorizacao
            .exigirUsuarioAutorizadoParaAlterarExame(id);

        service.alterarPorTexto(
            id,
            usuarioId,
            request.status()
        );

        return ResponseEntity.noContent().build();
    }
}
