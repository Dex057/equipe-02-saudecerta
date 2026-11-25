package br.edu.ifpa.saudecerta.agenda.infra;

import br.edu.ifpa.saudecerta.agenda.domain.Consulta;
import br.edu.ifpa.saudecerta.agenda.domain.ConsultaRepository;
import br.edu.ifpa.saudecerta.agenda.domain.ConflitoDeHorarioException;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class ConsultaJpaAdapter implements ConsultaRepository {

    private static final String RESTRICAO_CONFLITO = "uk_consulta_profissional_horario_ocupado";

    private final ConsultaJpaRepository repository;

    ConsultaJpaAdapter(ConsultaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Consulta> buscar(Long id) {
        return repository.findById(id).map(ConsultaMapper::paraDominio);
    }

    @Override
    @Transactional
    public Optional<Consulta> buscarParaAtualizacao(Long id) {
        return repository.buscarParaAtualizacao(id).map(ConsultaMapper::paraDominio);
    }

    @Override
    @Transactional
    public Consulta salvar(Consulta consulta) {
        try {
            ConsultaEntity entity;
            if (consulta.getId() == null) {
                entity = new ConsultaEntity(consulta.getPacienteId(), consulta.getProfissionalId(),
                        consulta.getDataHora(), consulta.getStatus());
            } else {
                entity = repository.findById(consulta.getId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
                ConsultaMapper.atualizar(entity, consulta);
            }
            return ConsultaMapper.paraDominio(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            if (violouRestricaoDeHorario(exception)) {
                throw new ConflitoDeHorarioException("O profissional já possui consulta agendada nesse horário.");
            }
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeConflito(Long profissionalId, LocalDateTime dataHora, Long ignorarConsultaId) {
        return repository.existeConflito(profissionalId, dataHora, ignorarConsultaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Consulta> buscarPorProfissionalEData(Long profissionalId, LocalDate data) {
        return repository.findAllByProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHora(
                        profissionalId, data.atStartOfDay(), data.plusDays(1).atStartOfDay())
                .stream().map(ConsultaMapper::paraDominio).toList();
    }

    private static boolean violouRestricaoDeHorario(Throwable exception) {
        for (Throwable causa = exception; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao
                    && RESTRICAO_CONFLITO.equalsIgnoreCase(violacao.getConstraintName())) {
                return true;
            }
            if (causa instanceof SQLException sql && "23505".equals(sql.getSQLState())
                    && sql.getMessage() != null
                    && sql.getMessage().toLowerCase().contains(RESTRICAO_CONFLITO)) {
                return true;
            }
        }
        return false;
    }
}
