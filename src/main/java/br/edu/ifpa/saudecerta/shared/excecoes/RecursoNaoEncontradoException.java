package br.edu.ifpa.saudecerta.shared.excecoes;

/** Vira HTTP 404 com o código "NAO_ENCONTRADO" (ver GlobalExceptionHandler). */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
