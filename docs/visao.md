# SaúdeCerta — Documento de Visão (E1)

**Disciplina:** TEADS0347 — Desenvolvimento de Aplicações Multicamadas
**Turma:** NOITE C795NL
**Docente:** Márcio Goes do Nascimento
**Período:** 2026.2
**Entrega:** E1 — Documento de Visão e Requisitos

**Equipe:**
| Nome completo |
|---|
| Edson Davi Farias Martins |
| Marcus Carreira Júnior |
| Vergílio Junior |

**Repositório Git:** `https://github.com/Dex057/equipe-02-saudecerta`

---

> Requisitos, regras de negócio e drivers: [`requisitos.md`](requisitos.md).

## 1. Visão do produto

O SaúdeCerta é um sistema web para apoiar o funcionamento da Clínica SaúdeCerta, uma unidade de saúde de médio porte com cerca de vinte profissionais atendendo em especialidades diferentes. O objetivo é substituir o controle atual, feito em planilha para a agenda e em pasta física para o prontuário, por um sistema único que cubra o fluxo completo do atendimento: desde a marcação da consulta até o registro clínico e a prescrição.

A proposta não é informatizar tudo de uma vez. O sistema nasce cobrindo o essencial — cadastro, agenda, atendimento, prontuário e controle de acesso — e é pensado para crescer por módulos depois, sem precisar ser reescrito. Isso importa porque a clínica pretende evoluir aos poucos (por exemplo, incluindo faturamento ou telemedicina no futuro), e a arquitetura precisa aguentar esse crescimento.

## 2. Descrição do problema

Hoje a recepção usa planilhas compartilhadas para marcar consultas. Isso já gerou mais de uma vez o mesmo profissional com dois pacientes marcados no mesmo horário, porque duas pessoas mexem na planilha ao mesmo tempo e não existe nenhuma trava contra isso. Quando um paciente remarca, alguém da recepção precisa refazer a marcação manualmente, o que consome tempo em um momento — o balcão — onde tempo é justamente o que falta.

O prontuário em papel traz um problema diferente: quando um paciente que já foi atendido antes retorna, o profissional depende de encontrar a pasta física certa para saber o histórico dele. Isso atrasa o atendimento e, em alguns casos, o profissional acaba atendendo sem ter acesso ao histórico completo. Existe ainda um problema mais sério por trás disso: como o prontuário é físico, não há nenhum registro de quem consultou os dados de um paciente e quando. Dado que se trata de dado de saúde, essa ausência de rastro é um risco real, não só um incômodo operacional.

Resumindo o que o sistema precisa resolver:
- Impedir marcação de consulta em horário já ocupado pelo mesmo profissional.
- Dar acesso rápido ao histórico do paciente no momento do atendimento.
- Registrar quem acessou ou alterou o prontuário de cada paciente.
- Reduzir o retrabalho da recepção ao lidar com remarcações e cancelamentos.

> **Tensão central:** quanto mais forte for o controle de segurança e auditoria sobre o prontuário, mais passos isso pode adicionar ao fluxo da recepção e ao tempo de resposta do sistema. A arquitetura vai precisar equilibrar isso, e não simplesmente maximizar segurança ignorando o resto.

## 3. Stakeholders

Nem todo stakeholder usa o sistema diretamente. Por isso separamos stakeholders (quem tem interesse ou é afetado pelo sistema) dos atores (quem efetivamente opera o sistema), listados na seção seguinte.

| Stakeholder | Interesse no projeto |
|---|---|
| Direção da clínica | Contratante do sistema; quer reduzir erros operacionais e ter visibilidade gerencial (relatórios). |
| Recepção | Vai operar o sistema no dia a dia para marcar e remarcar consultas; precisa que seja rápido. |
| Profissionais de saúde | Usam o sistema para consultar histórico, registrar atendimento e prescrever; dependem da confiabilidade dos dados. |
| Pacientes | Titulares dos dados armazenados; têm interesse direto na privacidade do prontuário e em poder ver seus próprios agendamentos. |
| Equipe de desenvolvimento (nós) | Responsável por entregar e manter a arquitetura ao longo do semestre. |
| Órgãos reguladores (LGPD) | Não usam o sistema, mas a LGPD (Lei 13.709/2018, art. 5º, II) classifica dado de saúde como dado pessoal sensível, o que reforça o driver de segurança/auditoria. |

## 4. Atores do sistema

| Ator | O que faz no sistema |
|---|---|
| Recepcionista | Cadastra pacientes, marca, remarca e cancela consultas. |
| Profissional de saúde | Consulta o histórico do paciente, registra atendimento, emite prescrição e solicita exames. |
| Paciente | Consulta seus próprios agendamentos e dados. |
| Administrador | Gerencia usuários, perfis de acesso e especialidades; acompanha relatórios e trilha de auditoria. |

**Persona de referência — Márcia, recepcionista.** Atende telefone e balcão ao mesmo tempo no horário de pico. Precisa marcar uma consulta em menos de um minuto e saber na hora se o horário já está ocupado. É ela quem define o que "rápido" significa em RNF03 e RNF06.

## 5. Escopo

**Dentro do escopo:** cadastro de pacientes, profissionais e especialidades; agenda e marcação de consultas com verificação de conflito; atendimento com prontuário eletrônico; prescrições e solicitação de exames; autenticação e autorização por perfil; trilha de auditoria de acesso ao prontuário; relatórios gerenciais básicos.

**Fora do escopo, por decisão:** faturamento e convênios; integração com laboratórios externos; telemedicina; assinatura digital ICP-Brasil. Ficam como pontos de extensão e servem para exercitar a modificabilidade (RNF05).

**Fora do escopo inicial, por decisão da equipe:** o paciente apenas consulta seus agendamentos e dados (RF14). Solicitar ou confirmar consulta pelo próprio paciente fica para depois, porque a marcação pela recepção é o fluxo que resolve o problema descrito na seção 2.

## 6. Estilo arquitetural inicial

**Decisão: arquitetura em camadas (apresentação, aplicação, domínio, persistência), organizada como monólito modular, com o domínio isolado da infraestrutura por interfaces (estilo hexagonal / ports and adapters).**

Justificativa:

A equipe é pequena e o prazo é de um semestre — um cenário de microsserviços desde o início adicionaria complexidade de deploy, comunicação entre serviços e observabilidade sem trazer benefício real nesta fase do projeto. Uma arquitetura em camadas resolve o que o caso pede agora: separar claramente a parte que expõe a API (apresentação), a que orquestra os casos de uso (aplicação), a que concentra as regras de negócio (domínio, onde entram RN01 a RN07) e a que fala com o banco (persistência).

A escolha por isolar o domínio da infraestrutura (estilo hexagonal) vem direto do driver de modificabilidade (RNF05): o sistema precisa crescer por módulos sem reescrita, e o guia do caso já indica que no Projeto Final vamos extrair um serviço (agenda ou prontuário) do monólito. Se o domínio depender de detalhes do Spring Data JPA ou de qualquer outra tecnologia de persistência, essa extração fica muito mais difícil. Definindo interfaces no domínio e implementando-as na camada de persistência, mantemos essa porta aberta desde já sem pagar o custo de operar microsserviços agora.

Também pensamos no driver de segurança e auditoria: como o RBAC e a trilha de auditoria são coisas que atravessam vários módulos (agenda, prontuário, cadastro), faz sentido tratá-los como uma camada transversal separada, em vez de espalhar essa lógica dentro de cada módulo de negócio.

Em resumo, a arquitetura em camadas com monólito modular atende ao tamanho da equipe e ao prazo da disciplina, e a separação por portas/domínio garante que a evolução prevista para as próximas entregas (extração de serviço no Projeto Final) não exija reescrever o que for feito agora.
