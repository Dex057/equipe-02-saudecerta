package br.edu.ifpa.saudecerta.agenda.api;

import br.edu.ifpa.saudecerta.agenda.ConsultaResumo;
import br.edu.ifpa.saudecerta.agenda.application.AgendaService;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class AgendaController {

    private final AgendaService agenda;

    public AgendaController(AgendaService agenda) {
        this.agenda = agenda;
    }

    @PostMapping("/consultas")
    public ResponseEntity<ConsultaResumo> marcar(@Valid @RequestBody MarcarConsultaRequest request) {
        ConsultaResumo consulta = agenda.marcar(request.pacienteId(), request.profissionalId(), request.dataHora());
        return ResponseEntity.created(URI.create("/api/consultas/" + consulta.id())).body(consulta);
    }

    @PatchMapping("/consultas/{id}")
    public ConsultaResumo atualizar(@PathVariable @Positive Long id,
                                    @Valid @RequestBody AtualizarConsultaRequest request) {
        boolean cancelar = "CANCELADA".equals(request.status());
        if (cancelar == (request.dataHora() != null)) {
            throw new RegraDeNegocioException("Informe dataHora para remarcar ou status CANCELADA para cancelar.");
        }
        return agenda.atualizar(id, request.dataHora(), cancelar);
    }

    @GetMapping("/profissionais/{id}/agenda")
    public List<LocalDateTime> agenda(@PathVariable @Positive Long id,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return agenda.horariosDisponiveis(id, data);
    }

    @GetMapping("/consultas")
    public List<ConsultaResumo> buscar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                                       @RequestParam @Positive Long profissionalId) {
        return agenda.buscar(data, profissionalId);
    }

    @GetMapping("/consultas/{id}")
    public ConsultaResumo buscarConsulta(@PathVariable @Positive Long id) {
        return agenda.buscarConsulta(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
    }
}
