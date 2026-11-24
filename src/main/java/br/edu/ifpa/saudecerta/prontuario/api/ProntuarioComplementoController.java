
package br.edu.ifpa.saudecerta.prontuario.api;

import java.util.Map;

import br.edu.ifpa.saudecerta.prontuario.application.AutorizacaoProntuario;
import br.edu.ifpa.saudecerta.prontuario.application.ComplementarAtendimentoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/atendimentos")
public class ProntuarioComplementoController {

    private final ComplementarAtendimentoService service;
    private final AutorizacaoProntuario autorizacao;

    public ProntuarioComplementoController(
            ComplementarAtendimentoService service,
            AutorizacaoProntuario autorizacao) {

        this.service = service;
        this.autorizacao = autorizacao;
    }

    @PostMapping("/{id}/complementos")
    public ResponseEntity<Map<String, Long>> complementar(
            @PathVariable Long id,
            @Valid @RequestBody ComplementarAtendimentoRequest request) {

        var profissional = autorizacao
            .exigirProfissionalAutorizadoParaComplementarAtendimento(id);

        Long complementoId = service.complementarRetornandoId(
            id,
            profissional.profissionalId(),
            profissional.usuarioId(),
            request.texto()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("id", complementoId));
    }
}
