package br.edu.ifpa.saudecerta.cadastro.domain;

import br.edu.ifpa.saudecerta.shared.excecoes.ConflitoException;

/** RN01: já existe paciente ativo com este CPF. Vira HTTP 409 (ADR-006). */
public class CpfDuplicadoException extends ConflitoException {

    public CpfDuplicadoException() {
        super("Já existe um paciente ativo com este CPF.");
    }
}
