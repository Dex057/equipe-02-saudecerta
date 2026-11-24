package br.edu.ifpa.saudecerta.cadastro.domain;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;

public final class Especialidade {

    private final Long id;
    private String nome;

    private Especialidade(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public static Especialidade nova(String nome) {
        return new Especialidade(null, validar(nome));
    }

    public static Especialidade reconstituir(Long id, String nome) {
        return new Especialidade(id, nome);
    }

    public Especialidade comId(Long novoId) {
        return new Especialidade(novoId, nome);
    }

    public void renomear(String novoNome) {
        this.nome = validar(novoNome);
    }

    private static String validar(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome da especialidade é obrigatório.");
        }
        return nome.trim();
    }

    public Long id() { return id; }
    public String nome() { return nome; }
}
