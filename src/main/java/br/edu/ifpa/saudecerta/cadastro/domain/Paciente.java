package br.edu.ifpa.saudecerta.cadastro.domain;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Locale;

/** Paciente. O CPF identifica o cadastro (RN01: não há dois ativos com o mesmo CPF). Nunca é excluído, só inativado. */
public final class Paciente {

    private final Long id;
    private String nome;
    private Cpf cpf;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private boolean ativo;

    private Paciente(Long id, String nome, Cpf cpf, LocalDate dataNascimento, String telefone, String email,
                     boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.email = email;
        this.ativo = ativo;
    }

    public static Paciente novo(String nome, Cpf cpf, LocalDate dataNascimento, String telefone, String email,
                                LocalDate hoje) {
        Paciente p = new Paciente(null, null, null, null, null, null, true);
        p.atualizar(nome, cpf, dataNascimento, telefone, email, hoje);
        return p;
    }

    /** Reconstrói o paciente a partir do que está gravado (usado pelo adaptador de persistência). */
    public static Paciente reconstituir(Long id, String nome, Cpf cpf, LocalDate dataNascimento, String telefone,
                                        String email, boolean ativo) {
        return new Paciente(id, nome, cpf, dataNascimento, telefone, email, ativo);
    }

    public Paciente comId(Long novoId) {
        return new Paciente(novoId, nome, cpf, dataNascimento, telefone, email, ativo);
    }

    public void atualizar(String novoNome, Cpf novoCpf, LocalDate nascimento, String novoTelefone, String novoEmail,
                          LocalDate hoje) {
        if (novoNome == null || novoNome.isBlank()) {
            throw new RegraDeNegocioException("Nome é obrigatório.");
        }
        if (nascimento == null || nascimento.isAfter(hoje)) {
            throw new RegraDeNegocioException("Data de nascimento não pode estar no futuro.");
        }
        this.nome = novoNome.trim();
        this.cpf = novoCpf;
        this.dataNascimento = nascimento;
        this.telefone = novoTelefone;
        this.email = novoEmail;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    /** Minúsculas e sem acento, para a busca por prefixo do nome (RNF03). */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT);
    }

    public Long id() { return id; }
    public String nome() { return nome; }
    public Cpf cpf() { return cpf; }
    public LocalDate dataNascimento() { return dataNascimento; }
    public String telefone() { return telefone; }
    public String email() { return email; }
    public boolean ativo() { return ativo; }
}
