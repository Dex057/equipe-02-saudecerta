# ADR-006 — Banco relacional único com propriedade de tabelas por módulo

**Status:** Aceita (20/10/2026), revisada em 10/11/2026. Convenções das tabelas de custo/benefício: [README](README.md).

**Contexto.** RNF05 e o Projeto Final exigem que um módulo possa ser extraído futuramente. Se vários módulos fizerem `JOIN` e `FOREIGN KEY` livremente entre tabelas, a extração de um serviço exigirá desembaraçar dependências de persistência. Por outro lado, um banco por módulo desde o início aumentaria o custo operacional para uma clínica de aproximadamente 20 profissionais.

A arquitetura utiliza um monólito modular com PostgreSQL em produção e H2 em desenvolvimento e testes. Precisamos definir a propriedade das tabelas, os privilégios e como garantir a unicidade do CPF entre pacientes ativos (RN01), mantendo a compatibilidade entre os dois bancos.

**Decisão.**

1. **Banco de dados:** PostgreSQL em produção e H2 em desenvolvimento/testes. Todos os módulos compartilham um único banco de dados, mas mantêm fronteiras explícitas de propriedade.

2. **Propriedade por prefixo:** cada tabela pertence a um único módulo, identificado por prefixo:
   - `cad_` — Cadastro;
   - `age_` — Agenda;
   - `pro_` — Prontuário;
   - `seg_` — Segurança;
   - `aud_` — Auditoria.

   A escolha por prefixos, em vez de schemas separados, mantém o mesmo padrão no H2 e no PostgreSQL sem configuração adicional. A separação por schema fica como possibilidade de evolução no Projeto Final.

3. **CPF único entre pacientes ativos (RN01):** a tabela `cad_paciente` possui a coluna derivada `cpf_ativo`, preenchida com o CPF normalizado (11 dígitos) quando `ativo = true` e com `NULL` quando `ativo = false`.

   Uma restrição `UNIQUE (cpf_ativo)` impede dois pacientes ativos com o mesmo CPF. Como valores `NULL` não colidem nessa restrição, pacientes inativos podem compartilhar o CPF de outro cadastro.

   A técnica funciona em PostgreSQL e H2, seguindo o mesmo princípio da coluna `horario_ocupado` definida no ADR-005.

   Duas verificações complementares garantem a regra:
   - `CadastroService` consulta o repositório e identifica previamente um CPF ativo já cadastrado.
   - A restrição do banco impede duplicidade mesmo quando duas requisições concorrentes tentam cadastrar o mesmo CPF.

   O adaptador de persistência traduz a violação da restrição em `CpfDuplicadoException`, exceção de domínio que o `TratadorDeErros` converte em `409 Conflict`.

   A reativação de um paciente inativo também tenta ocupar novamente o valor de `cpf_ativo`, podendo resultar em `409` caso já exista outro cadastro ativo com o mesmo CPF.

4. **Fronteiras entre módulos:** nenhum módulo de negócio consulta diretamente tabelas pertencentes a outro. A comunicação utiliza as interfaces públicas `*Api` e referências por identificadores, como `pacienteId` e `profissionalId`.

   Não há chaves estrangeiras físicas entre módulos. Pacientes e profissionais são inativados, não excluídos definitivamente.

5. **Exceção de Relatórios:** o módulo `relatorios` pode consultar, somente para leitura e por consultas dedicadas, as tabelas `age_consulta`, `cad_profissional`, `cad_especialidade` e `cad_profissional_especialidade`, necessárias aos relatórios de RF13.

   Essa exceção não autoriza acesso irrestrito entre módulos.

6. **Verificação arquitetural:** testes ArchUnit verificarão:
   - Cada entidade JPA declara `@Table(name = "...")` com o prefixo correspondente ao módulo proprietário.
   - Somente classes de `relatorios.infra` podem usar `JdbcTemplate` ou SQL nativo para consultar tabelas pertencentes a outros módulos.
   - Os demais módulos acessam funcionalidades externas por interfaces públicas, evitando dependências diretas de persistência.

7. **Índices e restrições:**
   - `cad_paciente`: `UNIQUE (cpf_ativo)` para RN01.
   - `cad_paciente`: índice sobre nome normalizado para busca por prefixo (RNF03).
   - `age_consulta`: `UNIQUE (profissional_id, horario_ocupado)` para RN02, conforme ADR-005.
   - `aud_registro_auditoria`: índice sobre `(paciente_id, data_hora)` para consulta da trilha de auditoria.

### Tabelas, donos e privilégios

Os privilégios abaixo são concedidos ao usuário do banco utilizado pela aplicação em produção (PostgreSQL).

| Tabela | Módulo dono | A aplicação pode | Observação |
|---|---|---|---|
| `cad_paciente` | cadastro | SELECT, INSERT, UPDATE | Nunca DELETE; inativação pelo campo `ativo`; `cpf_ativo` com UNIQUE |
| `cad_profissional` | cadastro | SELECT, INSERT, UPDATE | Nunca DELETE; inativação |
| `cad_especialidade` | cadastro | SELECT, INSERT, UPDATE | Cadastro de especialidades |
| `cad_profissional_especialidade` | cadastro | SELECT, INSERT, DELETE | Tabela de ligação entre profissional e especialidade |
| `age_disponibilidade` | agenda | SELECT, INSERT, UPDATE, DELETE | A substituição da grade pode remover linhas antigas |
| `age_consulta` | agenda | SELECT, INSERT, UPDATE | Nunca DELETE; cancelamento por status; UNIQUE de horário e coluna de versão para concorrência |
| `pro_prontuario` | prontuario | SELECT, INSERT | Identidade do histórico do paciente |
| `pro_atendimento` | prontuario | SELECT, INSERT | Sem UPDATE ou DELETE; RN05 |
| `pro_complemento` | prontuario | SELECT, INSERT | Acrescenta informações sem alterar o atendimento original |
| `pro_prescricao` | prontuario | SELECT, INSERT | Sem UPDATE ou DELETE |
| `pro_item_prescricao` | prontuario | SELECT, INSERT | Sem UPDATE ou DELETE |
| `pro_solicitacao_exame` | prontuario | SELECT, INSERT, UPDATE somente em `status` | Agregado independente; RF09 e DC-1 |
| `seg_usuario` | seguranca | SELECT, INSERT, UPDATE | Nunca DELETE; desativação por `ativo` |
| `aud_registro_auditoria` | seguranca.auditoria | SELECT, INSERT | Trilha imutável; sem UPDATE ou DELETE |

**Privilégios no PostgreSQL:** a aplicação utiliza permissões restritas por tabela. Em `pro_solicitacao_exame`, a atualização fica limitada à coluna `status`, permitindo acompanhar a solicitação sem modificar o registro clínico original.

**Limitação do H2:** o ambiente de desenvolvimento/teste não reproduz o mesmo controle de privilégios por tabela e coluna adotado em produção. Nele, a imutabilidade será garantida pelas regras da aplicação e pelos testes. As restrições de privilégios da produção devem ser verificadas contra PostgreSQL.

**Verificação:** um teste de integração utilizando PostgreSQL em contêiner deverá conectar com o usuário da aplicação, tentar executar `UPDATE` em `pro_atendimento` e confirmar que a operação é rejeitada por falta de permissão.

**Exceção de Relatórios:** como existe um único usuário de banco para a aplicação, o isolamento da leitura de Relatórios não pode ser garantido exclusivamente pelos privilégios SQL. O acesso às tabelas de outros módulos fica restrito por regras arquiteturais verificadas com ArchUnit.

### Alternativas consideradas

- **A. Banco único com propriedade por módulo (escolhida).** Mantém baixo custo operacional, com fronteiras explícitas, prefixos e privilégios que favorecem uma futura extração.
- **B. Banco único sem restrições.** Permite JOINs e chaves estrangeiras livres, facilitando consultas e integridade referencial, mas aumenta o acoplamento entre módulos.
- **C. Um banco por módulo desde já.** Garante maior isolamento, porém exige mais infraestrutura, dificulta relatórios e elimina transações locais entre módulos.

| Critério (peso) | A. Único com propriedade | B. Único livre | C. Banco por módulo |
|---|---|---|---|
| Extração futura — RNF05 / PF (3) | 4 | 1 | 5 |
| Integridade referencial (2) | 3 | 5 | 1 |
| Custo operacional (2) | 4 | 5 | 1 |
| Desempenho de consultas e relatórios — RNF03 (2) | 4 | 5 | 2 |
| **Total ponderado** | **34** | **33** | **23** |

**Conferência dos cálculos:**

- A = (3 × 4) + (2 × 3) + (2 × 4) + (2 × 4) = **34**.
- B = (3 × 1) + (2 × 5) + (2 × 5) + (2 × 5) = **33**.
- C = (3 × 5) + (2 × 1) + (2 × 1) + (2 × 2) = **23**.

A e B ficam a apenas 1 ponto de diferença. B vence em integridade, simplicidade operacional e desempenho, mas perde fortemente no isolamento necessário à extração futura. A opção A mantém o maior resultado ponderado porque atende melhor ao RNF05 e ao Projeto Final.

As notas foram mantidas após a revisão, considerando que o uso de privilégios por coluna e os testes contra PostgreSQL aumentam o esforço da alternativa A, mas ainda não justificam mudar a classificação do custo operacional de 4 para 3. A avaliação desse custo deve ser confirmada pelo revisor.

### Consequências

(+) A propriedade das tabelas fica explícita, facilitando identificar quais dados deverão acompanhar cada módulo em uma futura extração.

(+) A regra RN01 é garantida pela aplicação e pelo banco, inclusive sob concorrência, utilizando a mesma técnica em H2 e PostgreSQL.

(+) A separação por prefixos reduz diferenças de configuração entre os bancos e permite validação por testes arquiteturais.

(+) A imutabilidade dos atendimentos, prescrições e registros de auditoria é reforçada por privilégios restritos em produção.

(+) `SolicitacaoDeExame` pode ter seu status atualizado sem alterar o conteúdo clínico original de `Atendimento`, preservando RN05 e atendendo RF09.

(−) O preenchimento de `cpf_ativo` depende da atualização correta pelo mapeador ao cadastrar, inativar ou reativar pacientes. **Mitigação:** teste de mapeamento de ida e volta e testes de unicidade na reativação.

(−) A ausência de chaves estrangeiras físicas entre módulos transfere parte da verificação de integridade para a aplicação. **Mitigação:** validação dos identificadores pelas APIs públicas e inativação em vez de exclusão definitiva.

(−) A exceção de Relatórios cria um acoplamento de leitura com Agenda e Cadastro. **Mitigação:** consultas dedicadas, somente leitura, verificadas com ArchUnit. Na extração futura, substituir por composição de APIs ou modelo de leitura específico.

(−) O H2 não reproduz integralmente os privilégios de produção. **Mitigação:** executar testes de integração contra PostgreSQL em contêiner, incluindo a tentativa de UPDATE proibido em `pro_atendimento`.

(−) A restrição de UPDATE por coluna em `pro_solicitacao_exame` acrescenta configuração de permissões e complexidade de testes. **Mitigação:** manter a migração SQL versionada e validar os privilégios com o usuário real da aplicação.

**Quando reavaliar:** no Projeto Final, ao extrair módulos para serviços independentes ou caso a manutenção das regras de isolamento no banco único se torne mais custosa do que separar os schemas ou bancos.

**Rastreia:** RF01, RF09, RF13, RN01, RN05, RNF03, RNF05.