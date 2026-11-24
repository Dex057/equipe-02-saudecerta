package br.edu.ifpa.saudecerta.shared.excecoes;

/** Vira HTTP 409 com o código "CONFLITO" (ver GlobalExceptionHandler). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
