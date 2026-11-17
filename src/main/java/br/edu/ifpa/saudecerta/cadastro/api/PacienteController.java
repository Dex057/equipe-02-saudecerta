package br.edu.ifpa.saudecerta.cadastro.api;

import br.edu.ifpa.saudecerta.cadastro.application.PacienteService;
import br.edu.ifpa.saudecerta.cadastro.application.PacienteView;
import br.edu.ifpa.saudecerta.shared.paginacao.Pagina;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PacienteView> cadastrar(@Valid @RequestBody PacienteRequest request) {
        PacienteView criado = service.cadastrar(request.paraDados());
        return ResponseEntity.created(URI.create("/api/pacientes/" + criado.id())).body(criado);
    }

    @GetMapping
    public Pagina<PacienteView> buscar(@RequestParam(defaultValue = "") String nome,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return service.buscarPorNome(nome, page, size);
    }

    @GetMapping("/{id}")
    public PacienteView obter(@PathVariable Long id) {
        return service.obter(id);
    }

    @PutMapping("/{id}")
    public PacienteView atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        return service.atualizar(id, request.paraDados());
    }
}
