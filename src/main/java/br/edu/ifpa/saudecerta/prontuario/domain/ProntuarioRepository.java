
package br.edu.ifpa.saudecerta.prontuario.domain;

public interface ProntuarioRepository {

    Long buscarOuCriarPorPaciente(Long pacienteId);
}
