# SaúdeCerta — Sistema de Gestão de Clínica

Estudo de caso da disciplina TEADS0347 — Desenvolvimento de Aplicações Multicamadas (IFPA, turma NOITE C795NL, 2026.2, Prof. Márcio Goes do Nascimento).

Sistema web multicamadas para digitalizar o fluxo de atendimento de uma clínica de médio porte: cadastro de pacientes e profissionais, agenda de consultas, prontuário eletrônico, prescrição/exames e controle de acesso com auditoria.

## Equipe

| Integrante | Frente no E2 | ADRs sob responsabilidade | Revisa os PRs de |
|---|---|---|---|
| Edson Davi Farias Martins ([@Dex057](https://github.com/Dex057)) | Coordenação, auditoria, implantação e cenários de qualidade | ADR-001, ADR-002 | Vergílio |
| Marcus Carreira Júnior ([@MarcusCarreiraJr](https://github.com/MarcusCarreiraJr)) | Diagramas C4 (draw.io) e modelo de domínio | ADR-003, ADR-006 | Edson |
| Vergílio Junior ([@vergiliojunior123-png](https://github.com/vergiliojunior123-png)) | Contrato REST, segurança e rastreabilidade | ADR-004, ADR-005 | Marcus |

## Entregas

Cada entrega acrescenta ao repositório e é marcada com uma tag.

- [x] **E1** — Visão e Requisitos (tag [`E1`](../../tree/E1)): [`docs/visao.md`](docs/visao.md), [`docs/requisitos.md`](docs/requisitos.md)
- [x] **E2** — Projeto Arquitetural (tag [`E2`](../../tree/E2)): [`docs/arquitetura.md`](docs/arquitetura.md), [`docs/adr/`](docs/adr/README.md), [`docs/rastreabilidade.md`](docs/rastreabilidade.md)
- [ ] **E3** — Protótipo em Spring Boot (`src/`)
- [ ] **Projeto Final** — Arquitetura baseada em serviços

## Cronograma do E2

| Até | Meta | Issues |
|---|---|---|
| Sáb 07/11 | Merge dos PRs de organização e de melhorias do E1; convites aceitos | PR #1, PR #2, #3 |
| Dom 08/11 | Decisões independentes prontas, com PR aberto | #4, #5, #7, #9, #10, #11, #12, #13 |
| Seg 09/11, manhã | Dependentes das anteriores | #6, #8 |
| Seg 09/11, noite | Matriz fechada e documentos lidos em sequência | #14, #15 (revisão) |
| Ter 10/11 | Merge, tag `E2` e link no AVA, antes do início da aula | #15 |

Se uma issue atrasar, avise na própria issue: a #14 só fecha depois das demais.

## Estrutura do repositório

```
docs/
  visao.md            visão, stakeholders, atores, escopo e estilo arquitetural (E1)
  requisitos.md       RF, RNF, RN, casos de uso e drivers (E1)
  arquitetura.md      C4, camadas, modelo de domínio e cenários de qualidade (E2)
  arquitetura.drawio  diagramas C4 editáveis (E2)
  img/                imagens exportadas do draw.io (E2)
  adr/                uma decisão arquitetural por arquivo (E2)
  rastreabilidade.md  matriz requisito → decisão → componente → teste (E2)
ref/                  material do professor
src/                  código Spring Boot (a partir do E3)
```

## Como trabalhamos

As tarefas ficam no [quadro do E2](https://github.com/users/Dex057/projects/2/views/2): Bloqueada → A fazer → Em andamento → Em revisão → Concluída. Para ver só as suas, filtre por `label:resp:seunome`.

1. Pegue uma issue sua e mova para "Em andamento". Cada issue diz o que fazer, em que branch e quando está pronta.
2. Crie a branch a partir da principal: `git checkout -b e2-nome-da-tarefa`.
3. Faça commits pequenos, com mensagem clara: `E2: adiciona alternativas ao ADR-004`.
4. Abra um Pull Request com `Closes #N` na descrição e peça revisão a quem revisa você (tabela acima). Mova a issue para "Em revisão".
5. Só entra na branch principal depois de revisado. No fim da entrega: `git tag E2 && git push --tags`.

Nunca suba senha, chave ou `target/`. Dúvidas sobre a tarefa ficam na própria issue.

## E3: como executar

Requisitos: **JDK 17** (o projeto traz o Maven Wrapper, não precisa instalar o Maven).

```
./mvnw test              # roda todos os testes (no Windows: mvnw.cmd test)
./mvnw spring-boot:run   # sobe a API em http://localhost:8080
```

O banco de desenvolvimento é o H2 em memória, em modo PostgreSQL. O esquema vem das migrações em `src/main/resources/db/migration` (Flyway); o Hibernate só confere se as entidades batem (`ddl-auto: validate`).

### Como trabalhamos no E3

- **Cada pessoa é dona de um módulo** e só edita o próprio pacote em `src/main/java/br/edu/ifpa/saudecerta/`: Edson (`cadastro`, `seguranca.auditoria`), Vergílio (`agenda`), Marcus (`prontuario`). Mexer em `shared/`, `pom.xml` ou `application.yml` é um PR pequeno e à parte.
- **Um módulo só fala com outro pela interface pública** (`CadastroApi`, `AgendaApi` e os `*Resumo`, na raiz do pacote do módulo). O `ArquiteturaTest` quebra o build se alguém acessar `api`, `application`, `domain` ou `infra` de outro módulo, ou se o `domain` importar JPA ou Spring.
- **Migrações por faixa de versão**, para as branches não colidirem: cadastro V10–V19, agenda V20–V29, prontuário V30–V39, segurança V40–V49, auditoria V50–V59. Tabelas com o prefixo do módulo (`cad_`, `age_`, `pro_`, `seg_`, `aud_`), como no ADR-006.
- **Erros** seguem o envelope `{codigo, mensagem, timestamp}` e os status do `arquitetura.md` (400, 404, 409, 422). Lance `RecursoNaoEncontradoException`, `ConflitoException` ou `RegraDeNegocioException`; o `GlobalExceptionHandler` converte.
- **"Hoje" vem do `Clock` injetado**, nunca de `LocalDate.now()`, para o teste poder fixar a data (RN03).
- Branch `e3-<tarefa>`, PR com `Closes #N`, revisão por quem a tabela da equipe indica, em até 12 horas. Todo PR roda `./mvnw test` antes de ser aberto.
