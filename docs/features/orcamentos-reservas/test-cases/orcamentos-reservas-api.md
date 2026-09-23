# Casos de Teste API - Orçamentos e Reservas

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/orcamentos-reservas/orcamentos-reservas.md

## Resumo Executivo
- **Total de casos de teste:** 21
- **Executados:** 0/21 (0%)
- **Distribuição por prioridade:**
  - P0: 11
  - P1: 7
  - P2: 3
- **Critérios de aceite cobertos:** 11 de 11 (API)
- **Regras de negócio cobertas:** 7/8 — regra 8 (sem delete) é limitação
  (CT-021 `[-]`)
- **Observações gerais:** nenhum `[x]` — feature nova, sem client/infra.
  Contratos dependem de `schemas/orcamentos/*` e
  `schemas/common/error-response.json`. Estado pré-condição por `BudgetFixture`
  (idempotente) + transações/parcelas para cálculo de `spent`. `spent` atualiza
  conforme gasto — orçamento é calculado sob demanda, não persistido.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Contrato do response de sucesso do POST /budgets | P0 | Contrato | - | - |
| [ ] | CT-002 | Contrato do response de erro do POST /budgets | P0 | Contrato | - | - |
| [ ] | CT-003 | Contrato do response de sucesso do GET /budgets | P0 | Contrato | - | - |
| [ ] | CT-004 | Contrato do response de erro do GET /budgets | P0 | Contrato | - | - |
| [ ] | CT-005 | Contrato do response de sucesso do PUT /budgets/{id} | P0 | Contrato | - | - |
| [ ] | CT-006 | Contrato do response de erro do PUT /budgets/{id} | P0 | Contrato | - | - |
| [ ] | CT-007 | Criar orçamento de categoria EXPENSE com dados válidos | P0 | Positivo | CA-criar | #1,#2 |
| [ ] | CT-008 | Listar orçamentos sem período retorna todos | P0 | Positivo | CA-listar-todos | #6 |
| [ ] | CT-009 | Listar orçamentos filtrados por month e year | P0 | Positivo | CA-listar-periodo | #6 |
| [ ] | CT-010 | Atualizar amount retorna orçamento atualizado | P0 | Positivo | CA-atualizar | #7 |
| [ ] | CT-011 | spent soma despesas diretas e parcelas de cartão | P0 | Positivo | CA-spent | #4 |
| [ ] | CT-012 | Criar orçamento com categoria INCOME retorna 409 | P1 | Negativo | CA-categoria-income | #2 |
| [ ] | CT-013 | Criar orçamento duplicado retorna 409 budget.alreadyExists | P1 | Negativo | CA-duplicado | #3 |
| [ ] | CT-014 | month/year/amount inválidos retornam 400 VALIDATION_ERROR | P1 | Negativo | CA-validacao | #6 |
| [ ] | CT-015 | Atualizar com amount inválido retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-validacao-update | #7 |
| [ ] | CT-016 | Atualizar orçamento de outro usuário retorna 404 budget.notFound | P1 | Negativo | CA-404-budget | #7 |
| [ ] | CT-017 | Criar orçamento com categoria de outro usuário retorna 404 | P1 | Negativo | CA-404-category | #1 |
| [ ] | CT-018 | available negativiza quando spent supera amount | P1 | Negativo | CA-available-neg | #5 |
| [ ] | CT-019 | GET /budgets?month=13 devolve lista vazia sem erro | P2 | Borda | CA-periodo-invalido | #6 |
| [ ] | CT-020 | PUT amount mantém spent e available recalculados | P2 | Positivo | CA-atualizar | #4,#5,#7 |
| [-] | CT-021 | Remover orçamento não existe (sem DELETE) | P2 | Limitação | - | #8 |

---

### CT-001 - Contrato do response de sucesso do POST /budgets
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/orcamentos/budget-response.json` (a criar)
- **Pre-condicoes:** usuário com categoria `EXPENSE`
- **Passos:**
  1. Enviar `POST /budgets` com payload válido e único
  2. Validar o corpo (201) contra o schema estrito
- **Dados de entrada:** `BudgetBuilder` (categoria EXPENSE, mês/ano únicos, amount)
- **Resultado esperado:** HTTP 201; body `BudgetResponse` (`additionalProperties: false`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-002 - Contrato do response de erro do POST /budgets
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /budgets` com categoria `INCOME`
  2. Validar o corpo de erro (409) contra o schema
- **Dados de entrada:** payload com categoria INCOME
- **Resultado esperado:** HTTP 409; body `{timestamp,status,error,message,path}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-003 - Contrato do response de sucesso do GET /budgets
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/orcamentos/budget-list-response.json` (a criar)
- **Pre-condicoes:** usuário com orçamento criado via API
- **Passos:**
  1. Enviar `GET /budgets` autenticado
  2. Validar o corpo (lista) contra o schema estrito
- **Dados de entrada:** sem query params
- **Resultado esperado:** HTTP 200; body `BudgetResponse[]` (`additionalProperties: false`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-004 - Contrato do response de erro do GET /budgets
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /budgets` sem `Authorization`
  2. Validar o corpo de erro (401) contra o schema
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401; body conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-005 - Contrato do response de sucesso do PUT /budgets/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/orcamentos/budget-response.json` (a criar)
- **Pre-condicoes:** usuário com orçamento existente
- **Passos:**
  1. Enviar `PUT /budgets/{id}` com novo `amount`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** `{"amount": "<novo>"}`
- **Resultado esperado:** HTTP 200; body `BudgetResponse`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-006 - Contrato do response de erro do PUT /budgets/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `PUT /budgets/{id}` com `amount` ausente
  2. Validar o corpo de erro (400) contra o schema
- **Dados de entrada:** `{}` ou `{"amount": null}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR`; body conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-007 - Criar orçamento de categoria EXPENSE com dados válidos
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com categoria `EXPENSE`
- **Passos:**
  1. Enviar `POST /budgets` com valores únicos e válidos
  2. Asserir 201, `id` não-nulo e `spent`/`available`
- **Dados de entrada:** `BudgetBuilder` com categoryId EXPENSE, month/year, amount
- **Resultado esperado:** HTTP 201; `spent = 0`, `available = amount`
- **CA:** CA-criar - **Regra:** #1,#2
- **Observacoes:** coberto por `BudgetApiTest#deveCriarOrcamentoComCategoriaDeDespesa`

### CT-008 - Listar orçamentos sem período retorna todos
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com orçamentos em meses distintos (2+)
- **Passos:**
  1. Enviar `GET /budgets` sem parâmetros
  2. Asserir que todos os orçamentos do usuário estão presentes
- **Dados de entrada:** sem query params
- **Resultado esperado:** HTTP 200; lista com todos os orçamentos (sem paginação)
- **CA:** CA-listar-todos - **Regra:** #6
- **Observacoes:** coberto por `BudgetApiTest#deveListarTodosOsOrcamentos`

### CT-009 - Listar orçamentos filtrados por month e year
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com orçamentos em meses distintos
- **Passos:**
  1. Enviar `GET /budgets?month=9&year=2026`
  2. Asserir que só trazem orçamentos de setembro/2026
- **Dados de entrada:** `month=9&year=2026`
- **Resultado esperado:** HTTP 200; apenas os orçamentos do período
- **CA:** CA-listar-periodo - **Regra:** #6
- **Observacoes:** coberto por `BudgetApiTest#deveListarOrcamentosDoPeriodo`

### CT-010 - Atualizar amount retorna orçamento atualizado
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com orçamento existente
- **Passos:**
  1. Enviar `PUT /budgets/{id}` com novo `amount`
  2. Asserir 200 e `amount`/`available` atualizados
- **Dados de entrada:** `{"amount": "<novo>"}`
- **Resultado esperado:** HTTP 200; `amount` novo, `spent` preservado,
  `available` recalculado
- **CA:** CA-atualizar - **Regra:** #7
- **Observacoes:** coberto por `BudgetApiTest#deveAtualizarValorDoOrcamento`

### CT-011 - spent soma despesas diretas e parcelas de cartão
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com orçamento no mês + despesa direta `EXPENSE`
  e/ou compra parcelada na mesma categoria do período
- **Passos:**
  1. Criar transação `EXPENSE` na categoria do orçamento (mês)
  2. Criar compra parcelada na categoria (fatura do mês)
  3. Consultar `GET /budgets?month=&year=`
  4. Asserir `spent` = Σ transações + Σ parcelas; `available = amount − spent`
- **Dados de entrada:** `TransactionBuilder` + `PurchaseBuilder` na categoria
- **Resultado esperado:** HTTP 200; `spent` correto para a categoria no mês
- **CA:** CA-spent - **Regra:** #4
- **Observacoes:** coberto por `BudgetApiTest#deveCalcularGastoComTransacoesEParcelas`

### CT-012 - Criar orçamento com categoria INCOME retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário com categoria `INCOME`
- **Passos:**
  1. Enviar `POST /budgets` com categoryId de categoria INCOME
  2. Asserir 409 e mensagem
- **Dados de entrada:** payload com categoria INCOME
- **Resultado esperado:** HTTP 409 `category.mustBeExpenseForBudget`
- **CA:** CA-categoria-income - **Regra:** #2
- **Observacoes:** coberto por `BudgetApiTest#deveRejeitarOrcamentoComCategoriaDeReceita`

### CT-013 - Criar orçamento duplicado retorna 409 budget.alreadyExists
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário com orçamento existente (categoria, mês, ano)
- **Passos:**
  1. Enviar `POST /budgets` com a mesma combinação (categoria, mês, ano)
  2. Asserir 409
- **Dados de entrada:** mesma categoryId/month/year, amount diferente
- **Resultado esperado:** HTTP 409 `budget.alreadyExists`
- **CA:** CA-duplicado - **Regra:** #3
- **Observacoes:** coberto por `BudgetApiTest#deveRejeitarOrcamentoDuplicado`

### CT-014 - month/year/amount inválidos retornam 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /budgets` parametrizado: `month=0`/`month=13`/`amount=0`/campo ausente
  2. Asserir 400 e `fields` apontando o campo
- **Dados de entrada:** payloads parametrizados
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.<campo>`
- **CA:** CA-validacao - **Regra:** #6
- **Observacoes:** pode ser um CASO parametrizado (mensagens `month.between1and12`,
  `amount.greaterThanZero`, `*.required`)

### CT-015 - Atualizar com amount inválido retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário com orçamento existente
- **Passos:**
  1. Enviar `PUT /budgets/{id}` com `amount` ausente/0/negativo
  2. Asserir 400 e `fields.amount`
- **Dados de entrada:** `{"amount": 0}` / ausente
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`amount.required` /
  `amount.greaterThanZero`)
- **CA:** CA-validacao-update - **Regra:** #7
- **Observacoes:** -

### CT-016 - Atualizar orçamento de outro usuário retorna 404 budget.notFound
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário A com orçamento; usuário B autenticado
- **Passos:**
  1. Enviar `PUT /budgets/{id}` (token B) com o id do orçamento do A
  2. Asserir 404
- **Dados de entrada:** id do orçamento do A + token do B
- **Resultado esperado:** HTTP 404 `budget.notFound` (ownership)
- **CA:** CA-404-budget - **Regra:** #7
- **Observacoes:** ownership via `findByUser_IdAndId`

### CT-017 - Criar orçamento com categoria de outro usuário retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário B com categoria; usuário A autenticado
- **Passos:**
  1. Enviar `POST /budgets` (token A) com categoryId da categoria do B
  2. Asserir 404
- **Dados de entrada:** payload com categoria de outro usuário
- **Resultado esperado:** HTTP 404 `category.notFound`
- **CA:** CA-404-category - **Regra:** #1
- **Observacoes:** ownership da categoria no create

### CT-018 - available negativiza quando spent supera amount
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário com orçamento de valor baixo + gastos maiores na categoria
- **Passos:**
  1. Criar gastos (transações/parcelas) na categoria que superem o `amount`
  2. Consultar `GET /budgets`
  3. Asserir `available < 0`
- **Dados de entrada:** amount menor que os gastos somados
- **Resultado esperado:** `available` negativo; sem bloqueio de "estouro"
- **CA:** CA-available-neg - **Regra:** #5
- **Observacoes:** orçamento não bloqueia gastos

### CT-019 - GET /budgets?month=13 devolve lista vazia sem erro
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** usuário com orçamentos
- **Passos:**
  1. Enviar `GET /budgets?month=13&year=2026`
  2. Asserir 200 com lista vazia
- **Dados de entrada:** `month=13`
- **Resultado esperado:** HTTP 200; `[]` (filtro condicional, sem validação de faixa)
- **CA:** CA-periodo-invalido - **Regra:** #6
- **Observacoes:** comportamento **inconsistente** com o dashboard (que dá 500) — documentar no assert (200 + vazio)

### CT-020 - PUT amount mantém spent e available recalculados
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com orçamento e gastos na categoria (spent > 0)
- **Passos:**
  1. Enviar `PUT /budgets/{id}` com novo amount
  2. Asserir `amount` novo, `spent` inalterado, `available` recalcuilado
- **Dados de entrada:** novo `amount`
- **Resultado esperado:** HTTP 200; `available = amount − spent` (com spent do estado)
- **CA:** CA-atualizar - **Regra:** #4,#5,#7
- **Observacoes:** [SUPOSICAO] update não recalcula spent (deriva do estado);
  validar no teste

### CT-021 - Remover orçamento não existe (sem DELETE)
- **Prioridade:** P2
- **Tipo:** Limitação
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. [NA] tentar `DELETE /budgets/{id}`
- **Dados de entrada:** - 
- **Resultado esperado:** [NA] endpoint não existe; produto não oferece remoção
  de orçamento
- **CA:** - **Regra:** #8
- **Observacoes:** [-] limitação do app (regra 8). Não implementar; apenas
  registrar para negócio decidir.

---