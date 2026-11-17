package br.edu.ifpa.saudecerta.cadastro.domain;

import java.util.Objects;

/** CPF normalizado (11 dígitos) com os dígitos verificadores conferidos. */
public final class Cpf {

    private final String numero;

    private Cpf(String numero) {
        this.numero = numero;
    }

    /** Aceita com ou sem pontuação. Lança CpfInvalidoException se os dígitos não fecharem. */
    public static Cpf de(String bruto) {
        if (bruto == null || bruto.isBlank()) {
            throw new CpfInvalidoException("CPF é obrigatório.");
        }
        String digitos = bruto.replaceAll("\\D", "");
        if (!valido(digitos)) {
            throw new CpfInvalidoException("CPF inválido.");
        }
        return new Cpf(digitos);
    }

    static boolean valido(String d) {
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digito(d, 9) == d.charAt(9) - '0' && digito(d, 10) == d.charAt(10) - '0';
    }

    private static int digito(String d, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (d.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    /** Os 11 dígitos, sem pontuação. */
    public String numero() {
        return numero;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cpf outro && numero.equals(outro.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }
}
