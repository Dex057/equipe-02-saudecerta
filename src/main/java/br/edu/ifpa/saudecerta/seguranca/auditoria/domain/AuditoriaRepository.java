package br.edu.ifpa.saudecerta.seguranca.auditoria.domain;

import br.edu.ifpa.saudecerta.shared.paginacao.Pagina;

/** Porta de persistência da trilha. Só grava e lê; não há atualização nem exclusão. */
public interface AuditoriaRepository {

    RegistroAuditoria salvar(RegistroAuditoria registro);

    /** Mais recentes primeiro. */
    Pagina<RegistroAuditoria> consultar(FiltroAuditoria filtro, int pagina, int tamanho);
}
