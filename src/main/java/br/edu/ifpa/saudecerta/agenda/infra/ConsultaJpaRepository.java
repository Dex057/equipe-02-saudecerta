package br.edu.ifpa.saudecerta.agenda.infra;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ConsultaJpaRepository extends JpaRepository<ConsultaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ConsultaEntity c where c.id = :id")
    Optional<ConsultaEntity> buscarParaAtualizacao(@Param("id") Long id);

    @Query("""
            select count(c) > 0 from ConsultaEntity c
            where c.profissionalId = :profissionalId
              and c.dataHora = :dataHora
              and c.status = br.edu.ifpa.saudecerta.agenda.domain.StatusConsulta.AGENDADA
              and (:ignorarId is null or c.id <> :ignorarId)
            """)
    boolean existeConflito(@Param("profissionalId") Long profissionalId,
                           @Param("dataHora") LocalDateTime dataHora,
                           @Param("ignorarId") Long ignorarId);

    List<ConsultaEntity> findAllByProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHora(
            Long profissionalId, LocalDateTime inicio, LocalDateTime fim);
}
