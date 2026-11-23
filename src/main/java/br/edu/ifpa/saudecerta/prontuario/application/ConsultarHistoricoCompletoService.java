package br.edu.ifpa.saudecerta.prontuario.application;

import java.time.Instant;
import java.util.List;

import br.edu.ifpa.saudecerta.prontuario.domain.ComplementoRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.PrescricaoLeituraRepository;
import br.edu.ifpa.saudecerta.prontuario.domain.SolicitacaoDeExameRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compoe a pagina de atendimentos com complementos, prescricoes e exames.
 * O caso de uso base registra o evento de acesso e nao cria prontuarios ao consultar.
 */
@Service
public class ConsultarHistoricoCompletoService {

    private final ConsultarHistoricoService historico;
    private final ComplementoRepository complementos;
    private final PrescricaoLeituraRepository prescricoes;
    private final SolicitacaoDeExameRepository exames;

    public ConsultarHistoricoCompletoService(
            ConsultarHistoricoService historico,
            ComplementoRepository complementos,
            PrescricaoLeituraRepository prescricoes,
            SolicitacaoDeExameRepository exames) {
        this.historico = historico;
        this.complementos = complementos;
        this.prescricoes = prescricoes;
        this.exames = exames;
    }

    public record ComplementoResumo(
            Long id, Long profissionalId, String texto, Instant dataHora) {}

    public record ItemResumo(
            Long id, String medicamento, String dosagem, String posologia, String duracao) {}

    public record PrescricaoResumo(
            Long id, Instant dataHora, List<ItemResumo> itens) {}

    public record ExameResumo(Long id, String status) {}

    public record AtendimentoResumo(
            Long id, Long consultaId, String queixa, String evolucao,
            String conduta, Instant dataHora,
            List<ComplementoResumo> complementos,
            List<PrescricaoResumo> prescricoes,
            List<ExameResumo> exames) {}

    public record HistoricoResumo(
            Long pacienteId, int pagina, int tamanho, long total,
            List<AtendimentoResumo> atendimentos) {}

    @Transactional(readOnly = true)
    public HistoricoResumo consultarResumo(Long pacienteId, Long usuarioId, int pagina) {
        var resultado = historico.consultar(pacienteId, usuarioId, pagina);
        List<AtendimentoResumo> paginaCompleta = resultado.atendimentos().stream()
            .map(atendimento -> {
                Long id = atendimento.getId();

                List<ComplementoResumo> detalhes = complementos.buscarPorAtendimento(id)
                    .stream()
                    .map(c -> new ComplementoResumo(
                        c.getId(), c.getProfissionalId(), c.getTexto(), c.getDataHora()))
                    .toList();

                List<PrescricaoResumo> receitas = prescricoes.buscarPorAtendimento(id)
                    .stream()
                    .map(p -> new PrescricaoResumo(
                        p.getId(), p.getDataHora(),
                        p.getItens().stream()
                            .map(i -> new ItemResumo(i.getId(), i.getMedicamento(),
                                i.getDosagem(), i.getPosologia(), i.getDuracao()))
                            .toList()))
                    .toList();

                List<ExameResumo> solicitacoes = exames.buscarPorAtendimento(id)
                    .stream()
                    .map(e -> new ExameResumo(e.getId(), e.getStatus().name()))
                    .toList();

                return new AtendimentoResumo(
                    id, atendimento.getConsultaId(), atendimento.getQueixa(),
                    atendimento.getEvolucao(), atendimento.getConduta(),
                    atendimento.getDataHora(), detalhes, receitas, solicitacoes);
            })
            .toList();

        return new HistoricoResumo(resultado.pacienteId(), resultado.pagina(),
                resultado.tamanho(), resultado.total(), paginaCompleta);
    }
}
