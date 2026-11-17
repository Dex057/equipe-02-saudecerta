package br.edu.ifpa.saudecerta.cadastro.infra;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ProfissionalJpaRepository extends JpaRepository<ProfissionalJpa, Long> {

    List<ProfissionalJpa> findAllByOrderByNomeAsc();

    @Query("select p from ProfissionalJpa p join p.especialidadeIds e where e = :especialidadeId order by p.nome")
    List<ProfissionalJpa> findByEspecialidade(@Param("especialidadeId") Long especialidadeId);
}
