# ADR-003 — Domínio isolado da infraestrutura por portas (hexagonal)

**Status:** Aceita (20/10/2026). Convenções das tabelas de custo/benefício: [README](README.md).

**Contexto.** O guia lista como erros comuns o domínio anêmico, o vazamento de JPA no domínio e o controller gordo. As regras RN01–RN07 são o coração do sistema e precisam ser testáveis sem banco. A E1 já adotou o estilo hexagonal; este ADR registra o custo real dessa escolha.

**Decisão.** O domínio é composto de classes Java puras, com comportamento (`Consulta.cancelar()`, `Atendimento.complementar()`), sem anotações de Spring Data/JPA. O domínio declara **portas** (`ConsultaRepository`, `ProntuarioRepository`, `CadastroPort`, `ConsultaPort`); a camada `infra` as implementa com adaptadores JPA, com entidades JPA próprias e **mapeamento manual** entre elas.

**Alternativas consideradas.**

- **A. Domínio puro + portas + adaptadores JPA** (escolhida).
- **B. Camadas clássicas com entidades JPA como domínio:** `Controller → Service → Repository` Spring Data; as entidades carregam anotações e regras.
- **C. Domínio anêmico** (entidades só com getters/setters, regra no `Service`): o mais rápido, mas é o erro nº 1 da lista do guia.

| Critério (peso) | A. Hexagonal | B. JPA como domínio | C. Anêmico |
|---|---|---|---|
| Testabilidade das regras sem banco (3) | 5 | 3 | 2 |
| Modificabilidade e extração futura — RNF05 (3) | 5 | 2 | 1 |
| Esforço de implementação (2) | 2 | 4 | 5 |
| Curva de aprendizado da equipe (1) | 2 | 4 | 5 |
| **Total ponderado** | **36** | **27** | **24** |

**Consequências.**
(+) Regras RN02 e RN05 testadas com testes unitários puros, rápidos, exigidos no E3.
(+) Trocar a persistência ou extrair um serviço não mexe no domínio.
(−) **Custo:** para cada agregado, uma entidade JPA e um mapeador a mais; mais classes e risco de mapeamento esquecido. **Mitigação:** mapeamento manual simples (sem biblioteca extra), testes de mapeamento de ida e volta nos agregados com regra.
(−) Fere o "mínimo de código"; só se paga porque RNF05 e o PF exigem a fronteira.
**Quando reavaliar:** se algum módulo for só CRUD sem regra (hoje, `Especialidade`), aceitar entidade JPA direta como exceção documentada.

**Rastreia:** RF02, RF07, RF08, RF09, RN01–RN07, RNF05.
