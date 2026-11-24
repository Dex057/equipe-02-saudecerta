package br.edu.ifpa.saudecerta.seguranca.auditoria.infra;

import br.edu.ifpa.saudecerta.seguranca.auditoria.domain.AuditoriaRepository;
import br.edu.ifpa.saudecerta.seguranca.auditoria.domain.FiltroAuditoria;
import br.edu.ifpa.saudecerta.seguranca.auditoria.domain.RegistroAuditoria;
import br.edu.ifpa.saudecerta.seguranca.auditoria.domain.TipoAuditoria;
import br.edu.ifpa.saudecerta.shared.paginacao.Pagina;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
class AuditoriaJpaAdapter implements AuditoriaRepository {

    private final RegistroAuditoriaJpaRepository jpa;

    AuditoriaJpaAdapter(RegistroAuditoriaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public RegistroAuditoria salvar(RegistroAuditoria registro) {
        RegistroAuditoriaJpa e = new RegistroAuditoriaJpa();
        e.tipo = registro.tipo().name();
        e.pacienteId = registro.pacienteId();
        e.usuarioId = registro.usuarioId();
        e.acao = registro.acao();
        e.detalhe = registro.detalhe();
        e.dataHora = registro.dataHora();
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public Pagina<RegistroAuditoria> consultar(FiltroAuditoria filtro, int pagina, int tamanho) {
        Specification<RegistroAuditoriaJpa> spec = Specification.where(null);
        if (filtro.pacienteId() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("pacienteId"), filtro.pacienteId()));
        }
        if (filtro.usuarioId() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("usuarioId"), filtro.usuarioId()));
        }
        if (filtro.de() != null) {
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.<Instant>get("dataHora"), filtro.de()));
        }
        if (filtro.ate() != null) {
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.<Instant>get("dataHora"), filtro.ate()));
        }
        Page<RegistroAuditoriaJpa> resultado = jpa.findAll(spec,
                PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataHora", "id")));
        return Pagina.de(resultado.getContent().stream().map(AuditoriaJpaAdapter::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
    }

    private static RegistroAuditoria paraDominio(RegistroAuditoriaJpa e) {
        return new RegistroAuditoria(e.id, TipoAuditoria.valueOf(e.tipo), e.pacienteId, e.usuarioId, e.acao,
                e.detalhe, e.dataHora);
    }
}
