package br.edu.ifpa.saudecerta.prontuario.application;

import br.edu.ifpa.saudecerta.agenda.AgendaApi;
import br.edu.ifpa.saudecerta.prontuario.domain.AtendimentoRepository;
import br.edu.ifpa.saudecerta.shared.excecoes.RecursoNaoEncontradoException;
import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * IDENTIFICACAO TEMPORARIA PARA DEMONSTRACAO. O header X-Usuario-Id pode ser
 * fornecido por qualquer cliente e NAO autentica nem autoriza o acesso.
 * Substituir por uma policy baseada em JWT antes de qualquer implantacao real.
 */
@Component
public class AutorizacaoProntuarioPorHeader implements AutorizacaoProntuario {

    private final AtendimentoRepository atendimentos;
    private final ObjectProvider<AgendaApi> agendaProvider;

    public AutorizacaoProntuarioPorHeader(AtendimentoRepository atendimentos,
            ObjectProvider<AgendaApi> agendaProvider) {
        this.atendimentos = atendimentos;
        this.agendaProvider = agendaProvider;
    }

    @Override
    public Long exigirUsuarioAutorizadoParaRegistrarAtendimento(Long consultaId) {
        return usuarioIdDoHeader();
    }

    @Override
    public Long exigirUsuarioAutorizadoParaAlterarExame(Long exameId) {
        return usuarioIdDoHeader();
    }

    @Override
    public Long exigirUsuarioAutorizadoParaConsultarHistorico(Long pacienteId) {
        return usuarioIdDoHeader();
    }

    @Override
    public ProfissionalAutorizado exigirProfissionalAutorizadoParaComplementarAtendimento(Long atendimentoId) {
        var atendimento = atendimentos.buscarPorId(atendimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento nao encontrado."));
        AgendaApi agenda = agendaProvider.getIfAvailable();
        if (agenda == null) {
            throw new IllegalStateException("AgendaApi ainda nao integrada.");
        }
        var consulta = agenda.buscarConsulta(atendimento.getConsultaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta nao encontrada."));
        if (consulta.profissionalId() == null || consulta.profissionalId() <= 0) {
            throw new RegraDeNegocioException("Consulta sem profissional valido.");
        }
        return new ProfissionalAutorizado(consulta.profissionalId(), usuarioIdDoHeader());
    }

    private Long usuarioIdDoHeader() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servlet)) {
            return null;
        }
        String valor = servlet.getRequest().getHeader("X-Usuario-Id");
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(valor.trim());
            if (id <= 0) {
                throw new NumberFormatException("ID nao positivo");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException("X-Usuario-Id deve conter um numero positivo.");
        }
    }
}