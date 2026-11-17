package br.edu.ifpa.saudecerta.cadastro.infra;

import br.edu.ifpa.saudecerta.cadastro.domain.Especialidade;
import br.edu.ifpa.saudecerta.cadastro.domain.EspecialidadeRepository;
import br.edu.ifpa.saudecerta.shared.excecoes.ConflitoException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
class EspecialidadeJpaAdapter implements EspecialidadeRepository {

    private final EspecialidadeJpaRepository jpa;

    EspecialidadeJpaAdapter(EspecialidadeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Especialidade salvar(Especialidade especialidade) {
        EspecialidadeJpa e = new EspecialidadeJpa();
        e.id = especialidade.id();
        e.nome = especialidade.nome();
        try {
            return paraDominio(jpa.saveAndFlush(e));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflitoException("Já existe a especialidade \"" + especialidade.nome() + "\".");
        }
    }

    @Override
    public Optional<Especialidade> buscarPorId(Long id) {
        return jpa.findById(id).map(EspecialidadeJpaAdapter::paraDominio);
    }

    @Override
    public Optional<Especialidade> buscarPorNome(String nome) {
        return jpa.findByNome(nome).map(EspecialidadeJpaAdapter::paraDominio);
    }

    @Override
    public List<Especialidade> buscarPorIds(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(EspecialidadeJpaAdapter::paraDominio).toList();
    }

    @Override
    public List<Especialidade> listar() {
        return jpa.findAllByOrderByNomeAsc().stream().map(EspecialidadeJpaAdapter::paraDominio).toList();
    }

    private static Especialidade paraDominio(EspecialidadeJpa e) {
        return Especialidade.reconstituir(e.id, e.nome);
    }
}
