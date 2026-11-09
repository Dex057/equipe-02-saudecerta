# ADR-002 — Auditoria de acesso ao prontuário híbrida: assíncrona na leitura, síncrona na escrita

**Status:** Aceita (20/10/2026). Convenções das tabelas de custo/benefício: [README](README.md).

**Contexto.** RF12/RNF02 exigem registrar todo acesso ao prontuário; RNF03 exige leitura rápida. Há três situações diferentes: (1) **leitura**, a mais frequente; (2) **escrita** (atendimento, prescrição, exame), rara e já transacional; (3) **acesso negado**, rara e a mais importante para segurança. A meta de RNF02 é "disponível em até 1 minuto", o que já admite uma pequena defasagem. Um fato que a análise precisa admitir: com ~20 profissionais e um único banco, um `INSERT` síncrono por leitura custa poucos milissegundos, então o ganho de desempenho isolado é modesto. O argumento forte é outro: no Projeto Final o Prontuário pode virar um serviço separado, e aí a auditoria síncrona vira uma chamada remota no caminho crítico da leitura.

**Decisão.**

- **Leitura:** o `ProntuarioService` publica o evento `ProntuarioAcessado`; o `AuditoriaListener` (`@Async`, fila limitada com política *caller-runs*, para nunca descartar evento por estouro de fila) grava o `RegistroAuditoria`. Falha na gravação: 3 tentativas e, se persistir, registro em log estruturado `AUDITORIA_FALHA` com alerta. No desligamento, a fila é drenada (*graceful shutdown*).
- **Escrita:** o `RegistroAuditoria` é gravado **na mesma transação** da alteração. Não existe alteração sem rastro.
- **Acesso negado:** gravado de forma síncrona (`AcessoNegado`).
- **Política de falha:** a leitura segue (*fail-open*) mesmo se a auditoria falhar. Bloquear um profissional que precisa do histórico durante um atendimento é pior do que registrar com atraso; o risco é aceito e monitorado.

**Alternativas consideradas.**

- **A. Híbrida** (escolhida).
- **B. Tudo síncrono** na mesma transação: garantia máxima e implementação trivial, mas acopla Auditoria a cada caso de uso, põe um `INSERT` em toda leitura e inviabiliza a extração do PF sem transação distribuída.
- **C. Tudo assíncrono**, inclusive escrita: mais simples de uniformizar, mas pode perder o registro de uma alteração clínica numa queda de processo.
- **D. Outbox transacional:** o evento é gravado numa tabela na mesma transação e um processo o encaminha à auditoria. Garante não perder registro e prepara o PF, mas exige tabela, relay, reprocessamento e idempotência, e transforma a leitura em transação de escrita.

| Critério (peso) | A. Híbrida | B. Síncrona | C. Toda assíncrona | D. Outbox |
|---|---|---|---|---|
| Desempenho de leitura — RNF03 (2) | 4 | 3 | 4 | 3 |
| Garantia de não perder registro — RNF01/02 (3) | 3 | 5 | 2 | 5 |
| Simplicidade e prazo (3) | 4 | 5 | 4 | 2 |
| Desacoplamento e extração futura (3) | 4 | 2 | 4 | 5 |
| Isolamento: falha na auditoria não bloqueia o atendimento (2) | 4 | 1 | 4 | 3 |
| **Total ponderado** | **49** | **44** | **46** | **48** |

A diferença entre A e D é de 1 ponto, dentro da margem de erro da estimativa. O desempate é o prazo e a experiência da equipe (peso de simplicidade); D fica registrado como evolução natural.

**Consequências.**
(+) Leitura não espera a gravação da auditoria; Auditoria não é conhecida pelos módulos de negócio (só pelos eventos em `shared`).
(+) Escritas e tentativas negadas têm garantia forte.
(−) **Risco residual:** um evento de leitura em memória pode se perder se o processo for morto sem desligamento ordenado (janela de segundos). Mitigação: log estruturado de acesso HTTP (usuário + rota) como evidência redundante e independente.
(−) Duas rotas de gravação para manter e testar.
**Quando reavaliar (gatilho para migrar para D):** exigência regulatória de zero perda de registro de leitura, ou extração do Prontuário como serviço.

**Rastreia:** RF12, RF10, RF07, RNF01, RNF02, RNF03.
