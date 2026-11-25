package br.edu.ifpa.saudecerta.agenda.domain;

import br.edu.ifpa.saudecerta.shared.excecoes.ConflitoException;

public class ConflitoDeHorarioException extends ConflitoException {

    public ConflitoDeHorarioException(String mensagem) {
        super(mensagem);
    }
}
