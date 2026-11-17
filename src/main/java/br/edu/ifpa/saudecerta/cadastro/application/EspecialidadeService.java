package br.edu.ifpa.saudecerta.cadastro.application;

import br.edu.ifpa.saudecerta.cadastro.domain.Especialidade;
import br.edu.ifpa.saudecerta.cadastro.domain.EspecialidadeRepository;
import br.edu.ifpa.saudecerta.shared.excecoes.ConflitoException;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EspecialidadeService {

    private final EspecialidadeRepository especialidades;

    public EspecialidadeService(EspecialidadeRepository especialidades) {
        this.especialidades = especialidades;
    }

    @Transactional
    public EspecialidadeView cadastrar(String nome) {
        Especialidade nova = Especialidade.nova(nome);
        garantirNomeLivre(nova.nome(), null);
        return view(especialidades.salvar(nova));
    }

    @Transactional
    public EspecialidadeView atualizar(Long id, String nome) {
        Especialidade especialidade = especialidades.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Especialidade " + id + " não encontrada."));
        especialidade.renomear(nome);
        garantirNomeLivre(especialidade.nome(), id);
        return view(especialidades.salvar(especialidade));
    }

    @Transactional(readOnly = true)
    public List<EspecialidadeView> listar() {
        return especialidades.listar().stream().map(EspecialidadeService::view).toList();
    }

    private void garantirNomeLivre(String nome, Long ignorarId) {
        especialidades.buscarPorNome(nome)
                .filter(existente -> !existente.id().equals(ignorarId))
                .ifPresent(existente -> {
                    throw new ConflitoException("Já existe a especialidade \"" + nome + "\".");
                });
    }

    static EspecialidadeView view(Especialidade e) {
        return new EspecialidadeView(e.id(), e.nome());
    }
}
