package br.edu.ifpa.saudecerta.agenda.infra;

import br.edu.ifpa.saudecerta.agenda.domain.Consulta;

final class ConsultaMapper {

    private ConsultaMapper() {
    }

    static Consulta paraDominio(ConsultaEntity entity) {
        return new Consulta(entity.getId(), entity.getPacienteId(), entity.getProfissionalId(),
                entity.getDataHora(), entity.getStatus());
    }

    static void atualizar(ConsultaEntity entity, Consulta consulta) {
        entity.atualizar(consulta.getDataHora(), consulta.getStatus());
    }
}
