package br.edu.ifpa.saudecerta.agenda.application;

import br.edu.ifpa.saudecerta.agenda.AgendaApi;
import br.edu.ifpa.saudecerta.agenda.ConsultaResumo;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AgendaApiService implements AgendaApi {

    private final AgendaService agenda;

    public AgendaApiService(AgendaService agenda) {
        this.agenda = agenda;
    }

    @Override
    public Optional<ConsultaResumo> buscarConsulta(Long id) {
        return agenda.buscarConsulta(id);
    }

    @Override
    public void marcarRealizada(Long consultaId) {
        agenda.marcarRealizada(consultaId);
    }
}
