
package br.edu.ifpa.saudecerta.prontuario.application;

public interface AutorizacaoProntuario {

    Long exigirUsuarioAutorizadoParaAlterarExame(
        Long exameId
    );
}
