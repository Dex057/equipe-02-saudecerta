# ADR-005 — Conflito de horário: validação no domínio mais restrição única no banco

**Status:** Aceita (20/10/2026). Convenções das tabelas de custo/benefício: [README](README.md).

**Contexto.** Este é o problema que originou o projeto: a planilha permitiu o mesmo profissional com dois pacientes no mesmo horário porque duas pessoas escreviam ao mesmo tempo. Uma validação só na aplicação (ler "o horário está livre?" e depois gravar) tem uma janela de corrida: dois pedidos simultâneos leem "livre" e ambos gravam. RN02 só vale para consultas **AGENDADA**: uma consulta cancelada libera o horário (RF05). Pela premissa P3, "mesmo horário" é a mesma data/hora de início.

**Decisão.** Duas linhas de defesa:

1. **Domínio:** `ValidadorDeConflito.validarConflito()` consulta a porta `ConsultaRepository` antes de agendar e devolve erro de negócio com mensagem clara (caminho comum, sem custo de exceção de banco).
2. **Banco:** restrição `UNIQUE (profissional_id, horario_ocupado)`, onde `horario_ocupado` é igual a `data_hora` quando o status é AGENDADA e `NULL` caso contrário. Como `NULL` não colide em restrição única, consultas canceladas ou realizadas não bloqueiam o horário, e a técnica funciona igual em PostgreSQL e H2. O `ConsultaJpaAdapter` traduz a violação em `409 Conflict`.

**Alternativas consideradas.**

- **A. Domínio + UNIQUE no banco** (escolhida).
- **B. Só validação na aplicação** (verifica e depois grava): simples e com boa mensagem, mas **não** impede a corrida, que é exatamente o defeito a corrigir.
- **C. Lock pessimista** (`SELECT ... FOR UPDATE` na linha do profissional ou da agenda): correto, porém serializa todas as marcações do profissional e acrescenta transação mais longa.
- **D. Lock otimista** (`@Version`): marcar é um *insert*, não há linha existente para versionar; seria preciso criar uma linha-pai artificial só para isso.

| Critério (peso) | A. Domínio + UNIQUE | B. Só aplicação | C. Lock pessimista | D. Lock otimista |
|---|---|---|---|---|
| Correção sob concorrência (3) | 5 | 1 | 4 | 4 |
| Simplicidade (2) | 4 | 5 | 3 | 3 |
| Desempenho e contenção (1) | 5 | 5 | 3 | 4 |
| Mensagem de erro amigável — RNF06 (2) | 4 | 5 | 4 | 3 |
| Portabilidade H2 / PostgreSQL (1) | 4 | 5 | 4 | 4 |
| **Total ponderado** | **40** | **33** | **33** | **32** |

**Consequências.**
(+) A garantia é do banco: mesmo um bug futuro na aplicação não gera duplicidade.
(+) O caminho comum devolve mensagem amigável imediata, o que ajuda a recepção (RNF06).
(−) A regra existe em dois lugares (domínio e esquema); precisam andar juntas. **Mitigação:** um único teste de concorrência com duas threads exercita os dois (um `201`, um `409`).
(−) A coluna `horario_ocupado` é derivada; o mapeador precisa preenchê-la ao mudar o status.
**Limite conhecido:** funciona com horários de duração fixa (P3). Se um dia houver consultas de duração variável, trocar por restrição de exclusão de intervalos do PostgreSQL (`EXCLUDE USING gist`).

**Rastreia:** RF04, RF05, RF06, RN02, RNF06.
