package br.edu.ifpa.saudecerta.cadastro.infra;

import br.edu.ifpa.saudecerta.cadastro.domain.Cpf;
import br.edu.ifpa.saudecerta.cadastro.domain.CpfDuplicadoException;
import br.edu.ifpa.saudecerta.cadastro.domain.Paciente;
import br.edu.ifpa.saudecerta.cadastro.domain.PacienteRepository;
import br.edu.ifpa.saudecerta.shared.paginacao.Pagina;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
class PacienteJpaAdapter implements PacienteRepository {

    private static final String RESTRICAO_CPF_ATIVO = "cpf_ativo";

    private final PacienteJpaRepository jpa;

    PacienteJpaAdapter(PacienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Paciente salvar(Paciente paciente) {
        try {
            return paraDominio(jpa.saveAndFlush(paraEntidade(paciente)));
        } catch (DataIntegrityViolationException e) {
            String causa = String.valueOf(e.getMostSpecificCause().getMessage()).toLowerCase();
            if (causa.contains(RESTRICAO_CPF_ATIVO)) {
                throw new CpfDuplicadoException();
            }
            throw e;
        }
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        return jpa.findById(id).map(PacienteJpaAdapter::paraDominio);
    }

    @Override
    public Optional<Paciente> buscarAtivoPorCpf(Cpf cpf) {
        return jpa.findByCpfAtivo(cpf.numero()).map(PacienteJpaAdapter::paraDominio);
    }

    @Override
    public Pagina<Paciente> buscarPorNome(String prefixo, int pagina, int tamanho) {
        Page<PacienteJpa> resultado = jpa.findByNomeNormalizadoStartingWith(Paciente.normalizar(prefixo),
                PageRequest.of(pagina, tamanho, Sort.by("nomeNormalizado", "id")));
        return Pagina.de(resultado.getContent().stream().map(PacienteJpaAdapter::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }

    private static PacienteJpa paraEntidade(Paciente p) {
        PacienteJpa e = new PacienteJpa();
        e.id = p.id();
        e.nome = p.nome();
        e.nomeNormalizado = Paciente.normalizar(p.nome());
        e.cpf = p.cpf().numero();
        e.cpfAtivo = p.ativo() ? p.cpf().numero() : null;
        e.dataNascimento = p.dataNascimento();
        e.telefone = p.telefone();
        e.email = p.email();
        e.ativo = p.ativo();
        return e;
    }

    private static Paciente paraDominio(PacienteJpa e) {
        return Paciente.reconstituir(e.id, e.nome, Cpf.de(e.cpf), e.dataNascimento, e.telefone, e.email, e.ativo);
    }
}
