# ADR-006 — Banco relacional único com propriedade de tabelas por módulo

**Status:** Aceita (20/10/2026). Convenções das tabelas de custo/benefício: [README](README.md).

**Contexto.** RNF05 e o PF pedem que um módulo possa ser extraído. Se vários módulos fizerem `JOIN` e `FOREIGN KEY` livremente entre tabelas, extrair um deles significa desembaraçar o banco. Por outro lado, um banco por módulo agora seria custo operacional sem retorno para uma clínica de 20 profissionais. O guia sugere H2 em desenvolvimento e PostgreSQL opcional.

**Decisão.**

- **PostgreSQL** em produção; **H2** em desenvolvimento e testes (premissa: as diferenças são contidas, por isso o ADR-005 usa a técnica de `NULL` em vez de índice parcial, que o H2 não tem).
- **Um banco**, mas cada tabela pertence a **um** módulo (prefixo ou *schema* por módulo). Nenhum módulo acessa tabela de outro: usa a `*Api` do dono.
- Referências entre módulos são só por identificador (`paciente_id`), **sem FK física entre módulos**. O risco de referência solta é baixo porque o sistema nunca exclui paciente ou profissional, apenas os inativa (RN01 fala em "cadastro ativo").
- **Exceção explícita:** `relatorios` pode ler tabelas de outros módulos, somente leitura e por consultas dedicadas, porque seus totais cruzam agenda, profissional e especialidade (RF13). Fica registrada e restrita no ArchUnit.
- Índices: CPF único entre ativos (RN01), nome de paciente normalizado (minúsculas, sem acento) para busca por prefixo, `consulta (profissional_id, data_hora)`, `registro_auditoria (paciente_id, data_hora)`. Em produção, o usuário do banco da aplicação **não** tem `UPDATE`/`DELETE` em `registro_auditoria` nem em `atendimento` (reforço de RN05 e da imutabilidade da trilha).

**Alternativas consideradas.**

- **A. Banco único com propriedade por módulo** (escolhida).
- **B. Banco único sem restrições** (`JOIN` e FK livres): o mais simples e com a melhor integridade referencial, mas deixa o acoplamento de dados escondido e dificulta a extração.
- **C. Um banco por módulo desde já:** isolamento total, mas custo operacional alto, sem transação entre módulos e relatórios que viram composição de serviços.

| Critério (peso) | A. Único com propriedade | B. Único livre | C. Banco por módulo |
|---|---|---|---|
| Extração futura — RNF05 / PF (3) | 4 | 1 | 5 |
| Integridade referencial (2) | 3 | 5 | 1 |
| Custo operacional (2) | 4 | 5 | 1 |
| Desempenho de consultas e relatórios — RNF03 (2) | 4 | 5 | 2 |
| **Total ponderado** | **34** | **33** | **23** |

A e B ficam a 1 ponto. B vence em três critérios (integridade, custo e desempenho) e só perde onde o projeto mais precisa (extração, exigida no PF); por isso a nota ponderada não deve ser lida como diferença real, e sim como "A custa um pouco mais e compra o que o PF exige".

**Consequências.**
(+) Extrair um serviço significa mover tabelas já isoladas, não separar um emaranhado.
(+) A auditoria e o prontuário ficam protegidos contra alteração por privilégio do banco, além do código.
(−) Sem FK entre módulos, a integridade depende da aplicação e da regra de inativar em vez de excluir.
(−) O módulo Relatórios é uma exceção que precisará ser trocada por composição de APIs ou modelo de leitura replicado se Agenda ou Cadastro forem extraídos.
(−) Desenvolver em H2 e rodar em PostgreSQL pode esconder diferenças. **Mitigação:** rodar a suíte de integração também contra PostgreSQL (contêiner) antes das entregas.

**Rastreia:** RF01, RF13, RN01, RN05, RNF03, RNF05.
