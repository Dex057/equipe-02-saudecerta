# ADR-004 — Controle de acesso por perfil (RBAC), titularidade e JWT por turno

**Status:** Aceita (20/10/2026). Convenções das tabelas de custo/benefício: [README](README.md).

**Contexto.** RF11 exige acesso por perfil (4 perfis); RN06 exige que paciente só veja os próprios dados e que a recepção não veja conteúdo clínico. Isso é mais do que RBAC puro: "só o próprio" depende do recurso. Ao mesmo tempo, a recepção no horário de pico (RNF06) não pode sofrer passos extras de segurança.

**Decisão.**

1. **Modelo e camada:** RBAC com quatro perfis aplicado por `@PreAuthorize` nos casos de uso da camada de aplicação. A autenticação do JWT e as regras gerais de acesso HTTP ficam na configuração de Segurança; a autorização por operação e a `TitularidadePolicy` ficam na aplicação. A política compara o paciente do token com o paciente do recurso (RN06). O controller apenas traduz HTTP para o caso de uso: ele não decide autorização.
2. **Menor privilégio:** recepção e administrador não acessam conteúdo clínico. **Equilíbrio:** qualquer profissional pode abrir qualquer prontuário (a equipe é pequena e pode precisar cobrir colegas), mas **toda** abertura é auditada (ADR-002). Restringir a pacientes com consulta marcada fica como endurecimento futuro.
3. **Sessão do E3:** autenticação *stateless* com JWT de acesso válido por **8 horas**, no máximo um turno. As claims contêm o id do usuário (`sub`), perfil, id do vínculo, emissão e expiração. O login só emite token para `Usuario.ativo`; não há refresh token no E3. Não há reconfirmação por ação. A senha é armazenada com BCrypt (RNF07).
4. **Negativa e auditoria:** a camada transversal converte tanto negativas do filtro HTTP quanto `AccessDeniedException` de `@PreAuthorize` em um evento `AcessoNegado`, com usuário, ação/rota e, quando identificável sem consultar conteúdo clínico, id do paciente-alvo. Um handler comum para a cadeia de filtros e o `@ControllerAdvice` publica o evento; o listener síncrono de Auditoria tenta persistir o registro antes da resposta. O fluxo retorna `403` sem corpo clínico. Se a persistência falhar, o acesso continua negado, a falha é registrada como `AUDITORIA_FALHA` e gera alerta; o sistema não afirma que o registro foi persistido com sucesso. Prontuário e Segurança publicam o evento e não chamam `AuditoriaService` diretamente (ADR-002).

**Alternativas consideradas (modelo de autorização).**

- **A. RBAC + titularidade** (escolhida).
- **B. ABAC / motor de políticas externo** (ex.: OPA): regras muito flexíveis (horário, vínculo, finalidade), mas é infraestrutura nova para operar.
- **C. ACL por registro** (Spring ACL): permissão por prontuário, granularidade máxima, porém consulta ao banco a cada acesso e gestão trabalhosa.

| Critério (peso) | A. RBAC + titularidade | B. ABAC / OPA | C. ACL por registro |
|---|---|---|---|
| Segurança e granularidade (3) | 3 | 5 | 4 |
| Impacto em latência e usabilidade — RNF03/06 (2) | 5 | 4 | 3 |
| Simplicidade e prazo (3) | 5 | 1 | 2 |
| Aderência a RN06 (2) | 4 | 5 | 4 |
| **Total ponderado** | **42** | **36** | **32** |

**Alternativas consideradas (sessão).**

- **A. JWT de 8 horas, sem refresh no E3** (escolhida): não exige estado de sessão no servidor, é simples para a API do protótipo e evita novos logins durante um turno. O custo é real: se um token for roubado, pode ser usado até expirar; desativar um usuário não revoga tokens já emitidos, que podem continuar válidos por até 8 horas. O campo `Usuario.ativo` é conferido no login, não em um refresh inexistente.
- **B. JWT de 15–30 minutos com refresh token revogável:** reduz a janela de uso do token de acesso e permite encerrar a sessão por revogação do refresh, mas exige armazenamento, rotação e endpoint/fluxo de renovação. Fica para o Projeto Final.
- **C. Sessão no servidor (cookie HttpOnly):** permite revogação imediata e reduz exposição a roubo de token por script, mas exige estado compartilhado quando houver mais de um serviço.

| Critério (peso) | A. JWT 8 h | B. JWT curto + refresh | C. Sessão no servidor |
|---|---|---|---|
| Segurança / janela de revogação (3) | 2 | 4 | 5 |
| Usabilidade no turno — RNF06 (3) | 5 | 5 | 4 |
| Simplicidade e prazo do E3 (3) | 5 | 2 | 2 |
| **Total ponderado** | **36** | **33** | **33** |

A pontuação de segurança de A reconhece a janela maior de exposição, não a elimina. A escolha é pelo prazo e escopo do E3: uma API exercitada por Postman/Insomnia sem SPA nem refresh implementado. A duração de 8 horas limita o token a um turno e evita relogins repetidos, mas a equipe aceita e documenta o risco de revogação tardia. Reavaliar a duração e implementar refresh revogável no Projeto Final.

**Consequências.**
(+) Autorização por operação fica no caso de uso; a checagem de perfil e titularidade não acrescenta consulta ao banco nem passo por ação.
(+) Spring Security já faz o essencial; a política de titularidade é uma classe pequena.
(−) Granularidade limitada: dois profissionais veem os mesmos prontuários; só a auditoria dissuade abuso. Risco aceito e declarado.
(−) Revogação de acesso (ex.: funcionário desligado) não é imediata: tokens emitidos podem valer por até 8 horas. A recepção ganha no máximo um login por turno, mas a janela de exposição de um token roubado aumenta em relação a JWT de 15–30 minutos. Mitigação no E3: HTTPS, armazenamento seguro do bearer token pelo cliente e expiração obrigatória; `Usuario.ativo` é verificado no login. No Projeto Final, reavaliar refresh revogável ou sessão server-side.
(−) O evento de negativa é síncrono para preservar o rastro sem acoplar o módulo de negócio à Auditoria. Se o armazenamento falhar, o acesso permanece negado e a falha é registrada como `AUDITORIA_FALHA` com alerta; o registro persistido não é presumido (ADR-002).
**Quando reavaliar:** no Projeto Final, ao implementar refresh, ou antes se a clínica exigir revogação imediata ou segregação por especialidade.

**Rastreia:** RF11, RN06, RNF01, RNF06, RNF07.
