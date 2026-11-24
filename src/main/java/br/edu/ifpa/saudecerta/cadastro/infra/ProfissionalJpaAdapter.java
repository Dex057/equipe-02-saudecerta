package br.edu.ifpa.saudecerta.cadastro.infra;

import br.edu.ifpa.saudecerta.cadastro.domain.Profissional;
import br.edu.ifpa.saudecerta.cadastro.domain.ProfissionalRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class ProfissionalJpaAdapter implements ProfissionalRepository {

    private final ProfissionalJpaRepository jpa;

    ProfissionalJpaAdapter(ProfissionalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Profissional salvar(Profissional profissional) {
        ProfissionalJpa e = new ProfissionalJpa();
        e.id = profissional.id();
        e.nome = profissional.nome();
        e.ativo = profissional.ativo();
        e.especialidadeIds = new HashSet<>(profissional.especialidadeIds());
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public Optional<Profissional> buscarPorId(Long id) {
        return jpa.findById(id).map(ProfissionalJpaAdapter::paraDominio);
    }

    @Override
    public List<Profissional> listar(Long especialidadeId) {
        List<ProfissionalJpa> encontrados = especialidadeId == null
                ? jpa.findAllByOrderByNomeAsc()
                : jpa.findByEspecialidade(especialidadeId);
        return encontrados.stream().map(ProfissionalJpaAdapter::paraDominio).toList();
    }

    private static Profissional paraDominio(ProfissionalJpa e) {
        return Profissional.reconstituir(e.id, e.nome, e.especialidadeIds, e.ativo);
    }
}
