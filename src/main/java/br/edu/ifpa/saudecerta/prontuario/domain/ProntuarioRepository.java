

package br.edu.ifpa.saudecerta.prontuario.domain;

import java.util.Optional;

public interface ProntuarioRepository {

    Long buscarOuCriarPorPaciente(Long pacienteId);

    Optional<Long> buscarPacienteIdPorId(Long prontuarioId);
}
