package br.edu.ifpa.saudecerta.cadastro.infra;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface EspecialidadeJpaRepository extends JpaRepository<EspecialidadeJpa, Long> {

    Optional<EspecialidadeJpa> findByNome(String nome);

    List<EspecialidadeJpa> findAllByOrderByNomeAsc();
}
