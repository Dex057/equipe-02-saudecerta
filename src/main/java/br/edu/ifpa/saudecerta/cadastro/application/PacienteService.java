package br.edu.ifpa.saudecerta.cadastro.application;

import br.edu.ifpa.saudecerta.cadastro.domain.Cpf;
import br.edu.ifpa.saudecerta.cadastro.domain.CpfDuplicadoException;
import br.edu.ifpa.saudecerta.cadastro.domain.Paciente;
import br.edu.ifpa.saudecerta.cadastro.domain.PacienteRepository;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;
import br.edu.ifpa.saudecerta.shared.paginacao.Pagina;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    private static final int TAMANHO_MAXIMO = 100;

    private final PacienteRepository pacientes;
    private final Clock clock;

    public PacienteService(PacienteRepository pacientes, Clock clock) {
        this.pacientes = pacientes;
        this.clock = clock;
    }

    /** RF01. A checagem prévia dá a mensagem amigável; a restrição do banco cobre a corrida entre duas requisições. */
    @Transactional
    public PacienteView cadastrar(PacienteDados dados) {
        Cpf cpf = Cpf.de(dados.cpf());
        garantirCpfLivre(cpf, null);
        Paciente novo = Paciente.novo(dados.nome(), cpf, dados.dataNascimento(), dados.telefone(), dados.email(),
                LocalDate.now(clock));
        return view(pacientes.salvar(novo));
    }

    /** Atualiza os dados. Reativar um paciente inativo reaplica a RN01 e pode dar 409. */
    @Transactional
    public PacienteView atualizar(Long id, PacienteDados dados) {
        Paciente paciente = buscar(id);
        Cpf cpf = Cpf.de(dados.cpf());
        paciente.atualizar(dados.nome(), cpf, dados.dataNascimento(), dados.telefone(), dados.email(),
                LocalDate.now(clock));
        if (dados.ativo() != null) {
            if (dados.ativo()) {
                paciente.reativar();
            } else {
                paciente.inativar();
            }
        }
        if (paciente.ativo()) {
            garantirCpfLivre(cpf, id);
        }
        return view(pacientes.salvar(paciente));
    }

    @Transactional(readOnly = true)
    public PacienteView obter(Long id) {
        return view(buscar(id));
    }

    @Transactional(readOnly = true)
    public Pagina<PacienteView> buscarPorNome(String nome, int pagina, int tamanho) {
        int tamanhoSeguro = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        return pacientes.buscarPorNome(nome, Math.max(pagina, 0), tamanhoSeguro).mapear(PacienteService::view);
    }

    private Paciente buscar(Long id) {
        return pacientes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente " + id + " não encontrado."));
    }

    private void garantirCpfLivre(Cpf cpf, Long ignorarId) {
        pacientes.buscarAtivoPorCpf(cpf)
                .filter(existente -> !existente.id().equals(ignorarId))
                .ifPresent(existente -> {
                    throw new CpfDuplicadoException();
                });
    }

    static PacienteView view(Paciente p) {
        return new PacienteView(p.id(), p.nome(), p.cpf().numero(), p.dataNascimento(), p.telefone(), p.email(),
                p.ativo());
    }
}
