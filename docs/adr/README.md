# Decisões arquiteturais (ADRs)

- [ADR-001 — Monólito modular em vez de microsserviços no início](ADR-001-monolito-modular.md)
- [ADR-002 — Auditoria de acesso ao prontuário híbrida: assíncrona na leitura, síncrona na escrita](ADR-002-auditoria-hibrida.md)
- [ADR-003 — Domínio isolado da infraestrutura por portas (hexagonal)](ADR-003-dominio-hexagonal.md)
- [ADR-004 — Controle de acesso por perfil (RBAC) com regra de titularidade, e JWT de curta duração](ADR-004-rbac-jwt.md)
- [ADR-005 — Conflito de horário: validação no domínio mais restrição única no banco](ADR-005-conflito-de-horario.md)
- [ADR-006 — Banco relacional único com propriedade de tabelas por módulo](ADR-006-banco-por-modulo.md)

**Convenções.** Nas tabelas de custo/benefício, as notas vão de 1 (ruim) a 5 (ótimo) e, no critério de custo ou esforço, 5 significa **barato**. Os pesos (1 a 3) refletem a importância do critério para os drivers da E1. **São estimativas da equipe**, feitas antes do protótipo; devem ser revisadas no E3 com medições reais. Quando duas alternativas ficam a poucos pontos uma da outra, o texto diz isso e explica o desempate.
