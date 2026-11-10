
# SaúdeCerta — Matriz de rastreabilidade (E2)

Liga cada requisito à decisão que o sustenta, ao componente ou à tática que o implementa e à forma de verificar. Os nomes dos componentes são os mesmos dos diagramas de `arquitetura.md` (seção 2). Os testes listados são os **planejados** para o E3 e o Projeto Final.

| Requisito | Decisão (ADR) | Componente / tática | Como verificar |
|---|---|---|---|
| **RF01** Cadastrar pacientes | ADR-006 | `CadastroController` → `CadastroService` → `Paciente` + `Cpf`; porta `PacienteRepository`; índice único de CPF ativo | Cadastro válido aparece na busca; CPF repetido é rejeitado (RN01) |
| **RF02** Cadastrar profissionais e especialidades | ADR-003 | `Profissional` exige ao menos uma `Especialidade` | Teste unitário: salvar sem especialidade lança exceção |
| **RF03** Definir agenda do profissional | ADR-005 | `Disponibilidade.gerarHorarios()`; `ValidadorDeDisponibilidade` confere cada marcação/remarcação contra os slots | Faixa 08:00–12:00 com 30 min gera 8 horários; POST/PATCH fora da faixa ou da grade não grava |
| **RF04** Marcar consulta | ADR-005 | `AgendaService`, `Consulta` (status AGENDADA) | `POST /api/consultas` devolve `201` e `AGENDADA` |
| **RF05** Remarcar/cancelar | ADR-005 | `Consulta.remarcar()` / `cancelar()`; `horario_ocupado = NULL` libera o horário | Cancelar e marcar novamente o mesmo horário funciona |
| **RF06** Impedir conflito | ADR-005 | `ValidadorDeConflito.validarConflito()` + `UNIQUE (profissional_id, horario_ocupado)` | Teste unitário com dois agendamentos no mesmo horário; teste com duas threads (um `201`, um `409`) |
| **RF07** Registrar atendimento | ADR-003, ADR-002 | `ProntuarioService`, `Atendimento` imutável; auditoria de escrita na mesma transação | Atendimento salvo; tentativa de alterar ou apagar não tem operação disponível (RN05) |
| **RF08** Emitir prescrição | ADR-003 | `Prescricao` / `ItemDePrescricao` dentro do `Atendimento` | Prescrição sem atendimento é impossível (RN04); exige pelo menos um item |
| **RF09** Solicitar exames | ADR-003 | `SolicitacaoDeExame` dentro do `Atendimento` | Exame sem atendimento é impossível (RN04); status inicial SOLICITADO |
| **RF10** Consultar histórico | ADR-002 | `ProntuarioService.consultarHistorico` paginado; porta `ProntuarioRepository` | Ordem cronológica; p95 inferior a 2 segundos na primeira página |
| **RF11** Autenticar e aplicar perfis | ADR-004 | `SegurancaConfig`, `AuthController`, filtro JWT, `@PreAuthorize` | Teste da matriz perfil × recurso (`arquitetura.md`, seção 3.3) |
| **RF12** Auditar acesso ao prontuário | ADR-002 | `AuditoriaListener`, `AuditoriaService`, `RegistroAuditoria`, `AuditoriaController` | Acessar prontuário e verificar o registro, considerando a espera assíncrona |
| **RF13** Gerar relatórios | ADR-006 | `RelatoriosService` com consultas de leitura dedicadas | Totais corretos sobre massa de dados conhecida |
| **RF14** Consultar os próprios agendamentos e dados | — | `AgendaApi`, `CadastroApi` e `TitularidadePolicy` para restringir a consulta ao paciente autenticado | Paciente consulta os próprios dados e agendamentos; tentativa de consultar outro paciente é negada |
| **RF15** Gerenciar usuários e perfis | — | `Usuario`, `SegurancaConfig` e controle de perfis RBAC | ADMIN cria, edita e desativa usuários; usuário desativado não consegue autenticar |
| **RN01** CPF único entre pacientes ativos | ADR-003, ADR-006 | `Cpf`, `CadastroService` e índice único de CPF ativo | Dois pacientes ativos com o mesmo CPF são rejeitados; CPF de paciente inativo pode ser reutilizado conforme a regra |
| **RN02** Impedir consultas simultâneas no mesmo horário | ADR-003, ADR-005 | `ValidadorDeConflito` e `UNIQUE (profissional_id, horario_ocupado)` | Dois agendamentos para o mesmo profissional e horário: um é aceito e o outro rejeitado |
| **RN03** Impedir atendimento de consulta futura | ADR-003 | `Consulta.marcarRealizada(hoje)` com `Clock` injetado | Consulta futura não pode receber atendimento; teste com relógio fixo |
| **RN04** Prescrição e exame vinculados a atendimento | ADR-003 | `Atendimento` cria `Prescricao` e `SolicitacaoDeExame`; vínculo obrigatório | Não é possível criar prescrição ou solicitação de exame avulsa |
| **RN05** Prontuário somente-anexação | ADR-003, ADR-006 | `Atendimento` imutável, `ProntuarioRepository` sem operações de alteração ou exclusão | Não existe operação para apagar ou alterar atendimento; complementos são novos registros |
| **RN06** Restringir dados por titularidade e perfil | ADR-004 | `TitularidadePolicy` e `@PreAuthorize` | Paciente não acessa dados de outro paciente; recepção não acessa conteúdo clínico |
| **RN07** Cancelar somente consulta AGENDADA | ADR-003, ADR-005 | `Consulta.cancelar()` valida o status | Cancelar consulta REALIZADA ou CANCELADA resulta em erro |
| **RNF01** Segurança/Privacidade | ADR-004, ADR-002 | RBAC, `TitularidadePolicy`, `AcessoNegado` síncrono | Teste: todo perfil sem permissão recebe `403` e gera registro de acesso negado; cobertura de 100% da matriz |
| **RNF02** Auditabilidade | ADR-002 | Evento `ProntuarioAcessado` + listener assíncrono; trilha imutável | Registro visível ao administrador em até 1 minuto (meta de projeto: 5 segundos) |
| **RNF03** Desempenho | ADR-002, ADR-006 | Índice de nome, paginação, auditoria fora do caminho crítico | Medição de p95 da busca com 50 mil pacientes sintéticos (premissa P2) |
| **RNF04** Disponibilidade | ADR-001 | Monólito com `/actuator/health`, reinício automático, backup diário | Derrubar o processo e medir o tempo de recuperação; fila de auditoria drenada no desligamento |
| **RNF05** Modificabilidade | ADR-001, ADR-003, ADR-006 | Módulos, portas `*Api`, eventos em `shared`, tabelas por módulo | Testes ArchUnit; exercício: módulo `faturamento` vazio sem alterar `agenda` ou `prontuario` |
| **RNF06** Usabilidade | ADR-005, ADR-004 | `GET .../agenda` (horários livres), `POST /api/consultas` atômico, `409` imediato, sem reconfirmação | Roteiro cronometrado do UC01: até 1 minuto e até 5 cliques |
| **RNF07** Segurança de credenciais | ADR-004 | `BCryptPasswordEncoder`; `Usuario.senhaHash` | Teste: valor gravado começa com `$2`; revisão de código sem senha em texto puro |

## Cobertura inversa (ADR → requisitos)

Confere que nenhuma decisão arquitetural declarada nas ADRs fique sem requisito associado e que os requisitos não funcionais estejam vinculados às decisões correspondentes.

| ADR | Requisitos que sustenta |
|---|---|
| ADR-001 Monólito modular | RNF04, RNF05 |
<<<<<<< HEAD
| ADR-002 Auditoria híbrida | RF07, RF10, RF12, RNF01, RNF02, RNF03 |
| ADR-003 Hexagonal | RF01, RF02, RF03, RF07, RF08, RF09, RNF05 |
| ADR-004 RBAC + titularidade + JWT | RF10, RF11, RN06, RNF01, RNF06, RNF07 |
| ADR-005 Anti-conflito de horário | RF03, RF04, RF05, RF06, RN02, RNF06 |
| ADR-006 Banco com propriedade por módulo | RF01, RF13, RN05, RNF03, RNF05 |
=======
| ADR-002 Auditoria híbrida | RF12, RF10, RF07, RNF01, RNF02, RNF03 |
| ADR-003 Hexagonal | RF02, RF07, RF08, RF09, RN01–RN07, RNF05 |
| ADR-004 RBAC + titularidade + JWT | RF11, RN06, RNF01, RNF06, RNF07 |
| ADR-005 Anti-conflito de horário | RF04, RF05, RF06, RN02, RNF06 |
| ADR-006 Banco com propriedade por módulo | RF01, RF13, RN01, RN05, RNF03, RNF05 |
>>>>>>> 74934d7 (docs: atualiza matriz de rastreabilidade)

Os requisitos RF01 a RF15, as regras de negócio RN01–RN07 e os requisitos não funcionais RNF01–RNF07 possuem linhas próprias nesta matriz. A cobertura inversa reproduz as declarações **Rastreia** das ADRs existentes. RF14 e RF15 são requisitos complementares acrescentados para cobrir os objetivos dos atores Paciente e Administrador. RF03, RF14 e RF15 ainda não possuem uma ADR associada na matriz; essa associação deverá ser formalizada nas ADRs correspondentes caso a equipe confirme que as decisões arquiteturais existentes as sustentam. O local de aplicação e os testes das regras RN01–RN07 também estão detalhados em `arquitetura.md`, seção 4.2.
