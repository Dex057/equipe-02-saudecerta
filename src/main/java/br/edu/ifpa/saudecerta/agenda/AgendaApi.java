package br.edu.ifpa.saudecerta.agenda;

import java.util.Optional;

/**
 * Interface pública do módulo Agenda. O Prontuário a usa para localizar a consulta de um atendimento
 * e marcá-la como REALIZADA (RN03). A Agenda implementa.
 */
public interface AgendaApi {

    Optional<ConsultaResumo> buscarConsulta(Long id);

    /** Marca a consulta como REALIZADA. Falha com RegraDeNegocioException se a consulta for futura (RN03). */
    void marcarRealizada(Long consultaId);
}
