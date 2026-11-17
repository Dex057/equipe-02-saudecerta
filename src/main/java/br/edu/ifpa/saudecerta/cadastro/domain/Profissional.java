package br.edu.ifpa.saudecerta.cadastro.domain;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/** Profissional de saúde. RF02: precisa ter pelo menos uma especialidade. Nunca é excluído, só inativado. */
public final class Profissional {

    private final Long id;
    private String nome;
    private final Set<Long> especialidadeIds = new TreeSet<>();
    private boolean ativo;

    private Profissional(Long id, String nome, Collection<Long> especialidadeIds, boolean ativo) {
        this.id = id;
        this.ativo = ativo;
        atualizar(nome, especialidadeIds);
    }

    public static Profissional novo(String nome, Collection<Long> especialidadeIds) {
        return new Profissional(null, nome, especialidadeIds, true);
    }

    public static Profissional reconstituir(Long id, String nome, Collection<Long> especialidadeIds, boolean ativo) {
        return new Profissional(id, nome, especialidadeIds, ativo);
    }

    public Profissional comId(Long novoId) {
        return new Profissional(novoId, nome, especialidadeIds, ativo);
    }

    public void atualizar(String novoNome, Collection<Long> novasEspecialidades) {
        if (novoNome == null || novoNome.isBlank()) {
            throw new RegraDeNegocioException("Nome é obrigatório.");
        }
        if (novasEspecialidades == null || novasEspecialidades.isEmpty()) {
            throw new RegraDeNegocioException("O profissional precisa ter pelo menos uma especialidade.");
        }
        this.nome = novoNome.trim();
        this.especialidadeIds.clear();
        this.especialidadeIds.addAll(novasEspecialidades);
    }

    public void definirAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Long id() { return id; }
    public String nome() { return nome; }
    public Set<Long> especialidadeIds() { return Set.copyOf(especialidadeIds); }
    public boolean ativo() { return ativo; }
}
