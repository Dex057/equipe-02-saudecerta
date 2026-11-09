# ADR-004 — Controle de acesso por perfil (RBAC) com regra de titularidade, e JWT de curta duração

**Status:** Aceita (20/10/2026). Convenções das tabelas de custo/benefício: [README](README.md).

**Contexto.** RF11 exige acesso por perfil (4 perfis); RN06 exige que paciente só veja os próprios dados e que a recepção não veja conteúdo clínico. Isso é mais do que RBAC puro: "só o próprio" depende do recurso. Ao mesmo tempo, a recepção no horário de pico (RNF06) não pode sofrer passos extras de segurança.

**Decisão.**

1. **Modelo:** RBAC com quatro perfis, aplicado com `@PreAuthorize` na camada de aplicação, mais uma `TitularidadePolicy` que compara o paciente do token com o do recurso (RN06).
2. **Menor privilégio:** recepção e administrador não acessam conteúdo clínico. **Equilíbrio:** qualquer profissional pode abrir qualquer prontuário (a equipe é pequena e pode precisar cobrir colegas), mas **toda** abertura é auditada (ADR-002). Restringir a pacientes com consulta marcada fica como endurecimento futuro.
3. **Sessão:** autenticação *stateless* com JWT de curta duração; perfil e id do vínculo vão nas *claims*. Senha com BCrypt (RNF07). Nenhuma reconfirmação por ação.

**Alternativas consideradas (modelo de autorização).**

- **A. RBAC + titularidade** (escolhida).
- **B. ABAC / motor de políticas externo** (ex.: OPA): regras muito flexíveis (horário, vínculo, finalidade), mas é infraestrutura nova para operar.
- **C. ACL por registro** (Spring ACL): permissão por prontuário, granularidade máxima, porém consulta ao banco a cada acesso e gestão trabalhosa.

**Alternativas consideradas (sessão).**

- **JWT curto** (escolhida): sem estado no servidor, trafega entre serviços no PF. Custo: um token válido não pode ser revogado antes de expirar. Mitigação: expiração curta (ordem de 15 a 30 min) e renovação por *refresh token* guardado no servidor, que pode ser revogado (o refresh fica para o PF; o E3 usa só o token de acesso).
- **Sessão no servidor (cookie HttpOnly):** revogação imediata e menos exposição a roubo de token por script; porém exige estado compartilhado quando houver mais de um serviço.

| Critério (peso) | A. RBAC + titularidade | B. ABAC / OPA | C. ACL por registro |
|---|---|---|---|
| Segurança e granularidade (3) | 3 | 5 | 4 |
| Impacto em latência e usabilidade — RNF03/06 (2) | 5 | 4 | 3 |
| Simplicidade e prazo (3) | 5 | 1 | 2 |
| Aderência a RN06 (2) | 4 | 5 | 4 |
| **Total ponderado** | **42** | **36** | **32** |

**Consequências.**
(+) Checagem em memória: não acrescenta consulta ao banco nem passo para a recepção (resolve RNF01 × RNF06 sem sacrificar o prontuário).
(+) Spring Security já faz o essencial; a política de titularidade é uma classe pequena.
(−) Granularidade limitada: dois profissionais veem os mesmos prontuários; só a auditoria dissuade abuso. Risco aceito e declarado.
(−) Revogação de acesso (ex.: funcionário desligado) só vale após a expiração do token. Mitigação: expiração curta e `Usuario.ativo` conferido no refresh.
**Quando reavaliar:** quando a clínica crescer em número de profissionais, ou por exigência de segregação por especialidade.

**Rastreia:** RF11, RN06, RNF01, RNF06, RNF07.
