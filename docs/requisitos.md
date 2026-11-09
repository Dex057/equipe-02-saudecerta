# SaúdeCerta — Requisitos (E1)

> Visão, stakeholders, atores e estilo arquitetural: [`visao.md`](visao.md).

## 1. Requisitos funcionais

| ID | Requisito | Critério de aceitação |
|---|---|---|
| RF01 | Cadastrar e manter pacientes | Ao informar nome, CPF e data de nascimento válidos, o sistema salva o paciente e passa a listá-lo na busca. |
| RF02 | Cadastrar profissionais e vincular a especialidades | Um profissional precisa ter pelo menos uma especialidade; o cadastro não pode ser salvo sem isso. |
| RF03 | Definir a agenda do profissional | Profissional ou admin define dias e faixas de horário; o sistema gera os horários disponíveis a partir disso. |
| RF04 | Marcar consulta | Ao escolher profissional, data e horário livres, a consulta é criada com status AGENDADA. |
| RF05 | Remarcar ou cancelar consulta | Uma consulta AGENDADA pode ser remarcada ou cancelada; o horário liberado volta a ficar disponível. |
| RF06 | Impedir conflito de horário | O sistema recusa marcar uma consulta em horário já ocupado do mesmo profissional. |
| RF07 | Registrar atendimento no prontuário | O profissional registra queixa, evolução e conduta, vinculadas ao paciente e à consulta atendida. |
| RF08 | Emitir prescrição | Uma prescrição tem um ou mais medicamentos (nome, dose, posologia) e fica vinculada ao atendimento. |
| RF09 | Solicitar exames | O atendimento pode gerar uma ou mais solicitações de exame, cada uma com status de acompanhamento. |
| RF10 | Consultar histórico do paciente | O profissional visualiza, em ordem cronológica, os atendimentos, prescrições e exames anteriores do paciente. |
| RF11 | Autenticar usuários e aplicar perfis | O acesso a cada funcionalidade depende do perfil do usuário logado (recepção, profissional, admin, paciente). |
| RF12 | Registrar auditoria de acesso ao prontuário | Todo acesso ou alteração no prontuário grava quem fez, quando e em qual paciente. |
| RF13 | Gerar relatórios | O sistema gera relatórios de consultas por período, por profissional e por especialidade, com totais. |
| RF14 | Consultar os próprios agendamentos e dados | O paciente autenticado vê as próprias consultas e os próprios dados cadastrais; o pedido de dados de outra pessoa é negado (RN06). |
| RF15 | Gerenciar usuários e perfis | O administrador cria, edita e desativa usuários e atribui um perfil a cada um; usuário desativado não consegue autenticar. |

RF14 e RF15 não constam na lista do guia do caso. Foram acrescentados porque são objetivos dos atores Paciente e Administrador que nenhum outro requisito cobria.

## 2. Requisitos não funcionais

Estes são os requisitos que mais vão pesar nas decisões de arquitetura mais adiante (ver seção 5).

| ID | Atributo | Cenário de qualidade |
|---|---|---|
| RNF01 | Segurança/Privacidade | Um usuário sem perfil clínico tenta abrir um prontuário → o sistema nega o acesso e registra a tentativa → meta: 100% das tentativas indevidas bloqueadas. |
| RNF02 | Auditabilidade | Um profissional abre um prontuário → o sistema grava usuário, paciente, data/hora e ação → o registro fica disponível para o admin consultar em até 1 minuto. |
| RNF03 | Desempenho | Recepcionista busca um paciente pelo nome, com a base em uso normal → resultado retorna em menos de 2 segundos no percentil 95. |
| RNF04 | Disponibilidade | Durante o horário de funcionamento (12h/dia) → a agenda continua operante → disponibilidade mensal de pelo menos 99% nesse período. |
| RNF05 | Modificabilidade | Ao incluir um módulo novo (ex.: faturamento) no futuro → não deve ser necessário alterar o núcleo de agenda/prontuário → esforço estimado de no máximo 5 dias de equipe. |
| RNF06 | Usabilidade | Recepcionista marca uma consulta simples → conclui em até 1 minuto e no máximo 5 cliques a partir da tela inicial. |
| RNF07 | Segurança de credenciais | Senha nunca é armazenada em texto puro → o sistema usa hash com sal (BCrypt) → verificável em revisão de código. |

## 3. Regras de negócio

| ID | Regra |
|---|---|
| RN01 | Um paciente é identificado unicamente pelo CPF; não pode existir dois cadastros ativos com o mesmo CPF. |
| RN02 | Um profissional não pode ter duas consultas com status AGENDADA no mesmo horário. |
| RN03 | Só é possível registrar atendimento para consultas cuja data já ocorreu ou é a do dia atual. |
| RN04 | Prescrição e solicitação de exame só existem vinculadas a um atendimento — não existem de forma avulsa. |
| RN05 | O prontuário é somente-anexação: nenhum registro de atendimento é apagado, apenas complementado por novos registros. |
| RN06 | Paciente só enxerga os próprios dados; a recepção não tem acesso ao conteúdo clínico do prontuário. |
| RN07 | Cancelamento de consulta só é permitido se ela estiver com status AGENDADA. |

## 4. Casos de uso principais

### UC01 — Marcar consulta (ator: Recepcionista)

1. Busca o paciente ou o cadastra, se for novo (RF01).
2. Seleciona a especialidade e o profissional.
3. O sistema mostra os horários livres da agenda (RF03).
4. Escolhe data e horário e confirma.
5. O sistema valida o conflito (RF06, RN02) e cria a consulta como AGENDADA (RF04).

**Fluxo alternativo — horário ocupado:** se o horário foi ocupado entre os passos 3 e 5, por exemplo por outra recepcionista marcando ao mesmo tempo, o sistema informa o conflito e volta ao passo 3.

### UC02 — Atender paciente (ator: Profissional de saúde)

1. Abre a agenda do dia e seleciona a consulta.
2. O sistema registra o acesso ao prontuário (RF12, RNF02) e mostra o histórico (RF10).
3. Registra queixa, evolução e conduta (RF07).
4. Se necessário, emite prescrição (RF08) e solicita exames (RF09).
5. O atendimento é salvo e anexado ao prontuário (RN05).

**Fluxo alternativo — consulta futura:** se a data da consulta ainda não chegou, o sistema recusa o registro do atendimento (RN03).

## 5. Drivers arquiteturais

Nem todo requisito não funcional pesa igual na hora de desenhar a arquitetura. Olhando para os RNFs listados, destacamos três como drivers principais:

**Segurança e privacidade dos dados clínicos (RNF01, RNF07).** É dado de saúde, então esse requisito não é negociável e influencia praticamente todas as camadas do sistema — autenticação, controle de acesso por perfil (RBAC) e como o prontuário é exposto pela API.

**Auditabilidade do acesso ao prontuário (RNF02).** Diretamente ligado a RF12 e à regra RN06. É um driver porque não dá para tratar como um "log a mais": o guia do caso já deixa explícito que hoje não existe nenhum registro de quem acessa o prontuário físico, e isso é apontado como um dos riscos centrais do cenário atual.

**Desempenho na busca e no acesso a dados (RNF03).** Essencial para a recepção conseguir marcar consulta rápido (RNF06) e para o profissional abrir o prontuário sem esperar.

O conflito entre esses drivers é o ponto central da arquitetura: registrar auditoria de forma síncrona a cada leitura do prontuário atende RNF02, mas pode penalizar RNF03. Da mesma forma, um controle de acesso mais rígido reduz risco de segurança mas pode tornar o fluxo da recepção mais lento, o que vai contra RNF06. Essas decisões de tradeoff (por exemplo, auditoria assíncrona por eventos) serão detalhadas em ADRs na próxima entrega (E2); aqui o objetivo é só deixar registrado que esse conflito existe e que ele veio direto da análise dos requisitos.

Disponibilidade (RNF04) e modificabilidade (RNF05) são drivers secundários nesta fase: importam, mas não geram o mesmo nível de tensão entre si que segurança/auditoria versus desempenho/usabilidade.
