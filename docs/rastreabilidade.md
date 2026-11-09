# SaúdeCerta — Matriz de rastreabilidade (E2)

Liga cada requisito à decisão que o sustenta, ao componente/tática que o implementa e à forma de verificar. Os nomes de componentes são os mesmos dos diagramas de [`arquitetura.md`](arquitetura.md) (seção 2). Os testes listados são os **planejados** para o E3 e o Projeto Final.

| Requisito | Decisão (ADR) | Componente / tática | Como verificar |
|---|---|---|---|
| **RF01** Cadastrar pacientes | ADR-003, ADR-006 | `CadastroController` → `CadastroService` → `Paciente` + `Cpf`; porta `PacienteRepository`; índice único de CPF ativo | Cadastro válido aparece na busca; CPF repetido é rejeitado (RN01) |
| **RF02** Cadastrar profissionais e especialidades | ADR-003 | `Profissional` exige ao menos uma `Especialidade` | Teste unitário: salvar sem especialidade lança exceção |
| **RF03** Definir agenda do profissional | ADR-003 | `Disponibilidade.gerarHorarios()`; `AgendaController` (`GET .../agenda`) | Faixa 08:00–12:00 com 30 min gera 8 horários |
| **RF04** Marcar consulta | ADR-005 | `AgendaService`, `Consulta` (status AGENDADA) | `POST /api/consultas` devolve `201` e `AGENDADA` |
| **RF05** Remarcar/cancelar | ADR-005 | `Consulta.remarcar()` / `cancelar()`; `horario_ocupado = NULL` libera o horário | Cancelar e marcar de novo o mesmo horário funciona |
| **RF06** Impedir conflito | ADR-005 | `ValidadorDeConflito.validarConflito()` + `UNIQUE (profissional_id, horario_ocupado)` | Teste unitário com dois agendamentos no mesmo horário; teste com duas threads (um `201`, um `409`) |
| **RF07** Registrar atendimento | ADR-003, ADR-002 | `ProntuarioService`, `Atendimento` imutável; auditoria de escrita na mesma transação | Atendimento salvo; tentativa de alterar/apagar não tem operação (RN05) |
| **RF08** Emitir prescrição | ADR-003 | `Prescricao` / `ItemDePrescricao` dentro do `Atendimento` | Prescrição sem atendimento é impossível (RN04); exige ≥ 1 item |
| **RF09** Solicitar exames | ADR-003 | `SolicitacaoDeExame` dentro do `Atendimento` | Exame sem atendimento é impossível (RN04); status inicial SOLICITADO |
| **RF10** Consultar histórico | ADR-002, ADR-004 | `ProntuarioService.consultarHistorico` paginado; porta `ProntuarioRepository` | Ordem cronológica; p95 < 2 s na primeira página |
| **RF11** Autenticar e aplicar perfis | ADR-004 | `SegurancaConfig`, `AuthController`, filtro JWT, `@PreAuthorize` | Teste da matriz perfil × recurso (`arquitetura.md`, seção 3.3) |
| **RF12** Auditar acesso ao prontuário | ADR-002 | `AuditoriaListener`, `AuditoriaService`, `RegistroAuditoria`, `AuditoriaController` | Acessar prontuário e verificar o registro (com espera assíncrona) |
| **RF13** Gerar relatórios | ADR-006 | `RelatoriosService` com consultas de leitura dedicadas | Totais corretos sobre massa de dados conhecida |
| **RNF01** Segurança/Privacidade | ADR-004, ADR-002 | RBAC, `TitularidadePolicy`, `AcessoNegado` síncrono | Teste: todo perfil sem permissão recebe `403` e gera registro de negado; 100% da matriz |
| **RNF02** Auditabilidade | ADR-002 | Evento `ProntuarioAcessado` + listener assíncrono; trilha imutável | Registro visível ao admin em ≤ 1 min (meta de projeto: 5 s) |
| **RNF03** Desempenho | ADR-002, ADR-006 | Índice de nome, paginação, auditoria fora do caminho crítico | Medição de p95 da busca com 50 mil pacientes sintéticos (premissa P2) |
| **RNF04** Disponibilidade | ADR-001 | Monólito com `/actuator/health`, reinício automático, backup diário | Derrubar o processo e medir o tempo de recuperação; fila de auditoria drenada no desligamento |
| **RNF05** Modificabilidade | ADR-001, ADR-003, ADR-006 | Módulos, portas `*Api`, eventos em `shared`, tabelas por módulo | Testes ArchUnit; exercício: módulo `faturamento` vazio sem alterar `agenda`/`prontuario` |
| **RNF06** Usabilidade | ADR-005, ADR-004 | `GET .../agenda` (horários livres), `POST /api/consultas` atômico, `409` imediato, sem reconfirmação | Roteiro cronometrado do UC01: ≤ 1 min e ≤ 5 cliques |
| **RNF07** Segurança de credenciais | ADR-004 | `BCryptPasswordEncoder`; `Usuario.senhaHash` | Teste: valor gravado começa com `$2`; revisão de código sem senha em texto |

## Cobertura inversa (ADR → requisitos)

Confere que nenhuma decisão ficou sem requisito e nenhum RNF ficou sem decisão.

| ADR | Requisitos que sustenta |
|---|---|
| ADR-001 Monólito modular | RNF04, RNF05 |
| ADR-002 Auditoria híbrida | RF07, RF10, RF12, RNF01, RNF02, RNF03 |
| ADR-003 Hexagonal | RF01, RF02, RF03, RF07, RF08, RF09, RNF05 |
| ADR-004 RBAC + titularidade + JWT | RF10, RF11, RN06, RNF01, RNF06, RNF07 |
| ADR-005 Anti-conflito de horário | RF04, RF05, RF06, RN02, RNF06 |
| ADR-006 Banco com propriedade por módulo | RF01, RF13, RN05, RNF03, RNF05 |

Os requisitos RNF01 a RNF07 aparecem cada um em pelo menos uma ADR; os RF01 a RF13 também. As regras de negócio RN01–RN07 têm seu local de aplicação e teste em `arquitetura.md`, seção 4.2.
