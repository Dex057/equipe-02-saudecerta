package br.edu.ifpa.saudecerta.cadastro.domain;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;

/** CPF bem formado, mas com dígitos verificadores incorretos. */
public class CpfInvalidoException extends RegraDeNegocioException {

    public CpfInvalidoException(String mensagem) {
        super(mensagem);
    }
}
