package br.edu.ifpa.saudecerta.shared.excecoes;

import java.time.Instant;

/** Envelope de erro do contrato REST (arquitetura.md, seção 3.4). Nunca leva dado clínico nem token. */
public record RespostaErro(String codigo, String mensagem, Instant timestamp) {

    public static RespostaErro de(String codigo, String mensagem) {
        return new RespostaErro(codigo, mensagem, Instant.now());
    }
}
