package br.edu.ifpa.saudecerta.prontuario.api;

import java.util.Map;

import br.edu.ifpa.saudecerta.prontuario.application.AutorizacaoProntuario;
import br.edu.ifpa.saudecerta.prontuario.application.RegistrarAtendimentoFacade;
import jakarta.validation.Valid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultas")
@ConditionalOnProperty(prefix = "saudecerta.prontuario",
        name = "api-habilitada", havingValue = "true")
public class ProntuarioAtendimentoController {

    private final RegistrarAtendimentoFacade service;
    private final AutorizacaoProntuario autorizacao;

    public ProntuarioAtendimentoController(
            RegistrarAtendimentoFacade service,
            AutorizacaoProntuario autorizacao) {
        this.service = service;
        this.autorizacao = autorizacao;
    }

    @PostMapping("/{id}/atendimento")
    public ResponseEntity<Map<String, Long>> registrar(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarAtendimentoRequest request) {

        // Nao confiar em identificadores de usuario enviados pelo cliente.
        Long usuarioId = autorizacao.exigirUsuarioAutorizadoParaRegistrarAtendimento(id);
        Long atendimentoId = service.registrar(
                id, usuarioId, request.queixa(), request.evolucao(), request.conduta());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", atendimentoId));
    }
}
