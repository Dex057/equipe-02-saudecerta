package br.edu.ifpa.saudecerta.seguranca.auditoria.api;

import br.edu.ifpa.saudecerta.seguranca.auditoria.application.AuditoriaService;
import br.edu.ifpa.saudecerta.seguranca.auditoria.application.RegistroAuditoriaView;
import br.edu.ifpa.saudecerta.shared.paginacao.Pagina;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consulta da trilha (RF12, RNF02). Perfil ADMIN quando a Segurança entrar. */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    public Pagina<RegistroAuditoriaView> consultar(@RequestParam(required = false) Long pacienteId,
                                                   @RequestParam(required = false) Long usuarioId,
                                                   @RequestParam(required = false) Instant de,
                                                   @RequestParam(required = false) Instant ate,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return service.consultar(pacienteId, usuarioId, de, ate, page, size);
    }
}
