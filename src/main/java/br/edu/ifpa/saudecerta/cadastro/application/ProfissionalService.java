package br.edu.ifpa.saudecerta.cadastro.application;

import br.edu.ifpa.saudecerta.cadastro.domain.Especialidade;
import br.edu.ifpa.saudecerta.cadastro.domain.EspecialidadeRepository;
import br.edu.ifpa.saudecerta.cadastro.domain.Profissional;
import br.edu.ifpa.saudecerta.cadastro.domain.ProfissionalRepository;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfissionalService {

    private final ProfissionalRepository profissionais;
    private final EspecialidadeRepository especialidades;

    public ProfissionalService(ProfissionalRepository profissionais, EspecialidadeRepository especialidades) {
        this.profissionais = profissionais;
        this.especialidades = especialidades;
    }

    /** RF02: o profissional precisa de pelo menos uma especialidade, e todas têm de existir. */
    @Transactional
    public ProfissionalView cadastrar(ProfissionalDados dados) {
        Profissional novo = Profissional.novo(dados.nome(), dados.especialidadeIds());
        conferirEspecialidades(novo.especialidadeIds());
        Profissional salvo = profissionais.salvar(novo);
        return view(salvo);
    }

    @Transactional
    public ProfissionalView atualizar(Long id, ProfissionalDados dados) {
        Profissional profissional = profissionais.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional " + id + " não encontrado."));
        profissional.atualizar(dados.nome(), dados.especialidadeIds());
        conferirEspecialidades(profissional.especialidadeIds());
        if (dados.ativo() != null) {
            profissional.definirAtivo(dados.ativo());
        }
        return view(profissionais.salvar(profissional));
    }

    @Transactional(readOnly = true)
    public List<ProfissionalView> listar(Long especialidadeId) {
        return profissionais.listar(especialidadeId).stream().map(this::view).toList();
    }

    private void conferirEspecialidades(Set<Long> ids) {
        Set<Long> existentes = especialidades.buscarPorIds(ids).stream()
                .map(Especialidade::id).collect(Collectors.toSet());
        Set<Long> faltando = new HashSet<>(ids);
        faltando.removeAll(existentes);
        if (!faltando.isEmpty()) {
            throw new RegraDeNegocioException("Especialidade inexistente: " + faltando + ".");
        }
    }

    private ProfissionalView view(Profissional p) {
        Map<Long, Especialidade> porId = especialidades.buscarPorIds(p.especialidadeIds()).stream()
                .collect(Collectors.toMap(Especialidade::id, Function.identity()));
        List<EspecialidadeView> vistas = p.especialidadeIds().stream().sorted()
                .map(porId::get).map(EspecialidadeService::view).toList();
        return new ProfissionalView(p.id(), p.nome(), p.ativo(), vistas);
    }
}
