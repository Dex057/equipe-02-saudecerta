# ADR-005 — Conflito de horário: validação no domínio mais restrição única no banco

**Status:** Aceita (20/10/2026). Convenções das tabelas de custo/benefício: [README](README.md).

**Contexto.** Este é o problema que originou o projeto: a planilha permitiu o mesmo profissional com dois pacientes no mesmo horário porque duas pessoas escreviam ao mesmo tempo. Uma validação só na aplicação (ler "o horário está livre?" e depois gravar) tem uma janela de corrida: dois pedidos simultâneos leem "livre" e ambos gravam. RN02 só vale para consultas **AGENDADA**: uma consulta cancelada libera o horário (RF05). Pela premissa P3, "mesmo horário" é a mesma data/hora de início, e os horários válidos são os slots gerados a partir da disponibilidade do profissional.

**Decisão.** Três validações complementares e uma restrição de concorrência:

1. **Disponibilidade e grade (RF03/P3):** o `AgendaService`, na transação do comando, verifica que o profissional existe e está ativo, carrega as disponibilidades e pede ao domínio para gerar os slots daquela data. `ValidadorDeDisponibilidade` rejeita a data/hora se não coincidir exatamente com um slot gerado: início inclusivo, fim exclusivo e passo igual à duração configurada. Assim, um horário livre mas fora da agenda ou entre slots não pode ser marcado nem usado em remarcação. A violação é erro de negócio, não conflito.
2. **Domínio (RN02):** `ValidadorDeConflito.validarConflito()` consulta a porta `ConsultaRepository` antes de marcar ou remarcar e rejeita outro horário já ocupado com mensagem clara. É a validação do caminho comum; não é a garantia final sob concorrência.
3. **Banco (garantia concorrente):** restrição nomeada `uk_consulta_profissional_horario_ocupado` sobre `(profissional_id, horario_ocupado)`, onde `horario_ocupado` recebe `data_hora` quando o status é AGENDADA e `NULL` quando CANCELADA ou REALIZADA. `NULL` não colide em restrição única, portanto estados não AGENDADA não bloqueiam o slot. A mesma estratégia é usada em PostgreSQL e H2.
4. **Mapeamento de erro:** `ConsultaJpaAdapter` identifica especificamente a violação dessa restrição e lança `ConflitoDeHorarioException`, uma exceção da aplicação/domínio sem dependência de HTTP. O `@ControllerAdvice` converte essa exceção em `409 Conflict` para POST e PATCH. A infra não conhece status HTTP. A transação faz flush antes de responder, para que a violação seja mapeada antes do commit HTTP.
5. **Remarcação e cancelamento:** `PATCH /api/consultas/{id}` altera horário ou cancela em uma transação. Atualizações concorrentes da mesma consulta são serializadas pelo bloqueio da linha da consulta até o fim da transação; a restrição UNIQUE continua protegendo reservas de consultas diferentes. Uma remarcação bem-sucedida troca o slot e libera o anterior no commit. Se o novo slot estiver ocupado, a transação inteira é desfeita e a consulta permanece no slot antigo. Cancelar define `horario_ocupado = NULL`, liberando o slot no commit. Uma marcação concorrente que valida antes do commit ainda pode receber `409` e ser repetida; se a checagem ocorrer depois do commit, ela pode ocupar o slot. Em ambos os casos, a ordem de commit e a restrição do banco impedem duas consultas AGENDADA no mesmo slot. Cancelamento só é permitido para status AGENDADA (RN07); REALIZADA também não ocupa slot, mas não pode ser cancelada.

**Alternativas consideradas.**

- **A. Validadores de domínio + UNIQUE no banco** (escolhida): valida disponibilidade e conflito com mensagens de negócio; o banco fecha a janela de corrida.
- **B. Só validação na aplicação** (verifica e depois grava): simples e com boa mensagem, mas **não** impede a corrida, que é exatamente o defeito a corrigir.
- **C. Lock pessimista** (`SELECT ... FOR UPDATE` na linha do profissional ou da agenda): correto, porém serializa todas as marcações do profissional e acrescenta transação mais longa.
- **D. Lock otimista como garantia de agenda** (`@Version`): marcar é um *insert*, não há linha de slot existente para versionar; seria preciso criar uma linha-pai artificial. Uma versão na própria consulta pode detectar duas alterações simultâneas daquela consulta, mas não impede duas consultas diferentes de ocuparem o mesmo slot, então não substitui a UNIQUE.

| Critério (peso) | A. Validadores + UNIQUE | B. Só aplicação | C. Lock pessimista | D. Lock otimista |
|---|---|---|---|---|
| Correção sob concorrência (3) | 5 | 1 | 4 | 4 |
| Simplicidade (2) | 4 | 5 | 3 | 3 |
| Desempenho e contenção (1) | 5 | 5 | 3 | 4 |
| Mensagem de erro amigável — RNF06 (2) | 4 | 5 | 4 | 3 |
| Portabilidade H2 / PostgreSQL (1) | 4 | 5 | 4 | 4 |
| **Total ponderado** | **40** | **33** | **33** | **32** |

**Consequências.**
(+) A garantia de exclusão entre consultas diferentes é do banco: mesmo um bug futuro na validação de conflito não gera duplicidade.
(+) Validação de slot antes da gravação rejeita horário fora da agenda/grade; mensagem específica ajuda a recepção e não confunde regra de disponibilidade com conflito (RNF06).
(−) Regra de conflito existe no domínio e no esquema; precisam andar juntas. **Mitigação:** teste de integração com duas requisições concorrentes, uma `201` e uma `409`.
(−) A coluna `horario_ocupado` é derivada; o mapeador precisa atualizá-la em marcação, remarcação, cancelamento e conclusão.
(−) O bloqueio da linha serializa operações na mesma consulta; o controle da disputa entre consultas diferentes permanece na restrição UNIQUE.
**Limite conhecido:** funciona com horários de duração fixa (P3). Se um dia houver consultas de duração variável, trocar por restrição de exclusão de intervalos do PostgreSQL (`EXCLUDE USING gist`).

**Testes obrigatórios no E3 para RN02 e a regra de disponibilidade:**

- Unitários de `Disponibilidade.gerarHorarios()` e `ValidadorDeDisponibilidade`: início/fim da faixa, duração, intervalo entre slots, dia sem disponibilidade e data fora da faixa.
- Integração: POST em slot válido cria consulta; POST fora da faixa ou fora da grade não grava e devolve `422`.
- Integração: duas marcações concorrentes para profissional/slot iguais resultam em uma criação e uma `409`; repetir em PostgreSQL além de H2 valida a restrição real do banco.
- Integração: PATCH remarca para slot válido e libera o anterior após commit; PATCH para slot ocupado devolve `409`, mantém o horário anterior e não deixa estado parcial.
- Concorrência: duas remarcações para a mesma consulta são serializadas; cancelamento concorrente com nova marcação só torna o slot disponível após o commit, sem permitir duas consultas AGENDADA no mesmo slot.
- Integração: cancelamento de AGENDADA libera o slot, cancelamento de REALIZADA devolve `422`, e consultas CANCELADA/REALIZADA não bloqueiam o slot.

**Rastreia:** RF03, RF04, RF05, RF06, RN02, RNF06.
