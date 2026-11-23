package br.edu.ifpa.saudecerta.agenda.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultaRepository {

    Optional<Consulta> buscar(Long id);

    Optional<Consulta> buscarParaAtualizacao(Long id);

    Consulta salvar(Consulta consulta);

    boolean existeConflito(Long profissionalId, LocalDateTime dataHora, Long ignorarConsultaId);

    List<Consulta> buscarPorProfissionalEData(Long profissionalId, LocalDate data);
}
