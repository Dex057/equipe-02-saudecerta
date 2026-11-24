package br.edu.ifpa.saudecerta.agenda.application;

import br.edu.ifpa.saudecerta.agenda.ConsultaResumo;
import br.edu.ifpa.saudecerta.agenda.domain.Consulta;
import br.edu.ifpa.saudecerta.agenda.domain.ConsultaRepository;
import br.edu.ifpa.saudecerta.agenda.domain.StatusConsulta;
import br.edu.ifpa.saudecerta.agenda.domain.ValidadorDeConflito;
import br.edu.ifpa.saudecerta.agenda.domain.ValidadorDeDisponibilidade;
import br.edu.ifpa.saudecerta.cadastro.CadastroApi;
import br.edu.ifpa.saudecerta.cadastro.PacienteResumo;
import br.edu.ifpa.saudecerta.cadastro.ProfissionalResumo;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaService {

    private final ConsultaRepository consultas;
    private final ObjectProvider<CadastroApi> cadastroProvider;
    private final Clock clock;
    private final ValidadorDeConflito validadorDeConflito;
    private final ValidadorDeDisponibilidade validadorDeDisponibilidade = new ValidadorDeDisponibilidade();

    public AgendaService(ConsultaRepository consultas, ObjectProvider<CadastroApi> cadastroProvider, Clock clock) {
        this.consultas = consultas;
        this.cadastroProvider = cadastroProvider;
        this.clock = clock;
        this.validadorDeConflito = new ValidadorDeConflito(consultas);
    }

    @Transactional
    public ConsultaResumo marcar(Long pacienteId, Long profissionalId, LocalDateTime dataHora) {
        validarPaciente(pacienteId);
        validarProfissional(profissionalId);
        validadorDeDisponibilidade.validar(dataHora);
        validadorDeConflito.validarConflito(profissionalId, dataHora, null);
        return resumo(consultas.salvar(Consulta.agendada(pacienteId, profissionalId, dataHora)));
    }

    @Transactional
    public ConsultaResumo atualizar(Long id, LocalDateTime dataHora, boolean cancelar) {
        Consulta consulta = consultas.buscarParaAtualizacao(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
        if (cancelar) {
            consulta.cancelar();
        } else {
            validadorDeDisponibilidade.validar(dataHora);
            validadorDeConflito.validarConflito(consulta.getProfissionalId(), dataHora, id);
            consulta.remarcar(dataHora);
        }
        return resumo(consultas.salvar(consulta));
    }

    @Transactional(readOnly = true)
    public List<ConsultaResumo> buscar(LocalDate data, Long profissionalId) {
        validarProfissional(profissionalId);
        return consultas.buscarPorProfissionalEData(profissionalId, data).stream().map(AgendaService::resumo).toList();
    }

    @Transactional(readOnly = true)
    public List<LocalDateTime> horariosDisponiveis(Long profissionalId, LocalDate data) {
        validarProfissional(profissionalId);
        List<LocalDateTime> ocupados = consultas.buscarPorProfissionalEData(profissionalId, data).stream()
                .filter(consulta -> consulta.getStatus() == StatusConsulta.AGENDADA)
                .map(Consulta::getDataHora)
                .toList();
        return validadorDeDisponibilidade.gerarHorarios(data).stream()
                .filter(horario -> !ocupados.contains(horario))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ConsultaResumo> buscarConsulta(Long id) {
        return consultas.buscar(id).map(AgendaService::resumo);
    }

    @Transactional
    public void marcarRealizada(Long consultaId) {
        Consulta consulta = consultas.buscarParaAtualizacao(consultaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
        consulta.marcarRealizada(LocalDate.now(clock));
        consultas.salvar(consulta);
    }

    private void validarPaciente(Long id) {
        CadastroApi cadastro = cadastroProvider.getIfAvailable();
        if (cadastro == null) {
            throw new RegraDeNegocioException("Cadastro indisponível.");
        }
        Optional<PacienteResumo> paciente = cadastro.buscarPaciente(id);
        if (paciente.isEmpty() || !paciente.get().ativo()) {
            throw new RegraDeNegocioException("Paciente não encontrado ou inativo.");
        }
    }

    private void validarProfissional(Long id) {
        CadastroApi cadastro = cadastroProvider.getIfAvailable();
        if (cadastro == null) {
            throw new RegraDeNegocioException("Cadastro indisponível.");
        }
        Optional<ProfissionalResumo> profissional = cadastro.buscarProfissional(id);
        if (profissional.isEmpty() || !profissional.get().ativo()) {
            throw new RegraDeNegocioException("Profissional não encontrado ou inativo.");
        }
    }

    private static ConsultaResumo resumo(Consulta consulta) {
        return new ConsultaResumo(consulta.getId(), consulta.getPacienteId(), consulta.getProfissionalId(),
                consulta.getDataHora(), consulta.getStatus().name());
    }
}
