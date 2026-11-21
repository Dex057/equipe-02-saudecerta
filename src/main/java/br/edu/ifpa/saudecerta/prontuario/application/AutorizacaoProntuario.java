
package br.edu.ifpa.saudecerta.prontuario.application;

public interface AutorizacaoProntuario {

    Long exigirUsuarioAutorizadoParaAlterarExame(
        Long exameId
    );

    ProfissionalAutorizado
        exigirProfissionalAutorizadoParaComplementarAtendimento(
            Long atendimentoId
        );

    record ProfissionalAutorizado(
        Long profissionalId,
        Long usuarioId
    ) {}
}
