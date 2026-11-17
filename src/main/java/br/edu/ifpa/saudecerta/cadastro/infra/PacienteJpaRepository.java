package br.edu.ifpa.saudecerta.cadastro.infra;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface PacienteJpaRepository extends JpaRepository<PacienteJpa, Long> {

    Optional<PacienteJpa> findByCpfAtivo(String cpfAtivo);

    Page<PacienteJpa> findByNomeNormalizadoStartingWith(String prefixo, Pageable pageable);
}
