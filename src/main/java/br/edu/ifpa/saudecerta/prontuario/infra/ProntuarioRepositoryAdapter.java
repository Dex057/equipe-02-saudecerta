
package br.edu.ifpa.saudecerta.prontuario.infra;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpa.saudecerta.prontuario.domain.ProntuarioRepository;

@Repository
public class ProntuarioRepositoryAdapter
        implements ProntuarioRepository {

    private final ProntuarioJpaRepository jpaRepository;

    public ProntuarioRepositoryAdapter(
            ProntuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Long buscarOuCriarPorPaciente(Long pacienteId) {

        if (pacienteId == null || pacienteId <= 0) {
            throw new IllegalArgumentException(
                "Paciente invalido."
            );
        }

        return jpaRepository.findByPacienteId(pacienteId)
            .map(ProntuarioEntity::getId)
            .orElseGet(() -> {
                ProntuarioEntity novo =
                    new ProntuarioEntity(pacienteId);

                return jpaRepository
                    .saveAndFlush(novo)
                    .getId();
            });
    }
}

