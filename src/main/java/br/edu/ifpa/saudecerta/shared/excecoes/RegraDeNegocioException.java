package br.edu.ifpa.saudecerta.shared.excecoes;

/** Vira HTTP 422 com o código "REGRA_DE_NEGOCIO" (ver GlobalExceptionHandler). */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
