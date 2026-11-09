# ADR-001 — Monólito modular em vez de microsserviços no início

**Status:** Aceita (20/10/2026). Convenções das tabelas de custo/benefício: [README](README.md).

**Contexto.** A clínica tem cerca de 20 profissionais e a equipe tem 3 pessoas para um semestre. Os drivers que mais pesam aqui são entrega no prazo, RNF05 (crescer por módulos sem reescrita) e RNF04 (99% no expediente). O Projeto Final exige extrair pelo menos um serviço do monólito.

**Decisão.** Um único artefato implantável (a API), dividido em módulos de negócio com fronteiras explícitas: `cadastro`, `agenda`, `prontuario`, `relatorios`, `seguranca`. Módulos só se falam por interfaces `*Api` ou por eventos. As regras são verificadas por testes ArchUnit.

**Alternativas consideradas.**

- **A. Monólito modular** (escolhida).
- **B. Microsserviços desde o início.** Fronteiras e escala independentes, mas exigem rede entre serviços, deploy e observabilidade por serviço e consistência distribuída (marcar consulta + validar paciente viram chamadas remotas).
- **C. Monólito sem modularização.** O mais rápido de começar; mas o acoplamento cresce sem controle e a extração do PF vira reescrita.

| Critério (peso) | A. Monólito modular | B. Microsserviços | C. Monólito simples |
|---|---|---|---|
| Simplicidade de implantação e operação (3) | 4 | 1 | 5 |
| Velocidade de entrega no semestre (3) | 4 | 2 | 5 |
| Modificabilidade / extração futura — RNF05 (3) | 4 | 5 | 1 |
| Disponibilidade com recursos mínimos — RNF04 (1) | 4 | 2 | 4 |
| Isolamento de falhas e escala independente (1) | 2 | 5 | 2 |
| **Total ponderado** | **42** | **31** | **39** |

**Consequências.**
(+) Um único deploy e uma única transação cobrindo marcação + validação; fronteiras claras preparam a extração no PF.
(+) Custo de infraestrutura mínimo, compatível com a meta de RNF04.
(−) Uma falha derruba todos os módulos (sem isolamento); aceitável para a escala da clínica.
(−) Exige disciplina: sem ArchUnit, um `import` indevido desfaz a modularidade. **Mitigação:** testes de arquitetura rodando no build.
**Quando reavaliar:** se um módulo precisar escalar sozinho ou ter ciclo de deploy próprio (candidatos: Prontuário, Agenda).

**Rastreia:** RNF04, RNF05, E1 ([`visao.md`](../visao.md), seção 6).
