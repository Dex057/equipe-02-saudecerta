package br.edu.ifpa.saudecerta.cadastro.api;

import br.edu.ifpa.saudecerta.cadastro.application.EspecialidadeService;
import br.edu.ifpa.saudecerta.cadastro.application.EspecialidadeView;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService service;

    public EspecialidadeController(EspecialidadeService service) {
        this.service = service;
    }

    @GetMapping
    public List<EspecialidadeView> listar() {
        return service.listar();
    }

    @PostMapping
    public ResponseEntity<EspecialidadeView> cadastrar(@Valid @RequestBody EspecialidadeRequest request) {
        EspecialidadeView criada = service.cadastrar(request.nome());
        return ResponseEntity.created(URI.create("/api/especialidades/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public EspecialidadeView atualizar(@PathVariable Long id, @Valid @RequestBody EspecialidadeRequest request) {
        return service.atualizar(id, request.nome());
    }
}
