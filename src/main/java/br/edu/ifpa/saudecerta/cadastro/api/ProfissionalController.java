package br.edu.ifpa.saudecerta.cadastro.api;

import br.edu.ifpa.saudecerta.cadastro.application.ProfissionalService;
import br.edu.ifpa.saudecerta.cadastro.application.ProfissionalView;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
@RequestMapping("/api/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProfissionalView> listar(@RequestParam(required = false) Long especialidadeId) {
        return service.listar(especialidadeId);
    }

    @PostMapping
    public ResponseEntity<ProfissionalView> cadastrar(@Valid @RequestBody ProfissionalRequest request) {
        ProfissionalView criado = service.cadastrar(request.paraDados());
        return ResponseEntity.created(URI.create("/api/profissionais/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ProfissionalView atualizar(@PathVariable Long id, @Valid @RequestBody ProfissionalRequest request) {
        return service.atualizar(id, request.paraDados());
    }
}
