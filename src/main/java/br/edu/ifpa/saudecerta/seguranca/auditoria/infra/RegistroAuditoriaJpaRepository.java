package br.edu.ifpa.saudecerta.seguranca.auditoria.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface RegistroAuditoriaJpaRepository
        extends JpaRepository<RegistroAuditoriaJpa, Long>, JpaSpecificationExecutor<RegistroAuditoriaJpa> {
}
