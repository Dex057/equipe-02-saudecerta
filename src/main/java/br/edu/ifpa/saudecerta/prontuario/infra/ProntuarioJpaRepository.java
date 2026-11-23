
package br.edu.ifpa.saudecerta.prontuario.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProntuarioJpaRepository
        extends JpaRepository<ProntuarioEntity, Long> {

    Optional<ProntuarioEntity> findByPacienteId(Long pacienteId);

}
