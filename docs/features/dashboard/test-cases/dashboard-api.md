# Casos de Teste API - Dashboard

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/dashboard/dashboard.md

## Resumo Executivo
- **Total de casos de teste:** 15
- **Executados:** 0/15 (0%)
- **Distribuição por prioridade:**
  - P0: 7
  - P1: 6
  - P2: 2
- **Critérios de aceite cobertos:** 8 de 8 (API)
- **Regras de negócio cobertas:** 9/9
- **Observações gerais:** nenhum `[x]` — feature nova, sem client/infra.
  Contratos dependem de `schemas/dashboard/dashboard-response.json` e
  `schemas/common/error-response.json`. Pré-condições usam estados de outras
  features (contas, transações, compras/faturas, orçamentos) via fixtures. O
  quirk `month=13 → 500` é tratado como CT P2 **executável** (registrar o bug,
  não corrigi-lo). CT-014 usa paginação 0 na próxima fatura: sem faturas,
  `nextInvoice = null`.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Contrato do response de sucesso do GET /dashboard (sem dados) | P0 | Contrato | - | - |
| [ ] | CT-002 | Contrato do response de sucesso do GET /dashboard (com dados) | P0 | Contrato | - | - |
| [ ] | CT-003 | Contrato do response de erro do GET /dashboard | P0 | Contrato | - | - |
| [ ] | CT-004 | Dashboard sem parâmetros usa mês/ano atuais | P0 | Positivo | CA-sem-periodo | #1 |
| [ ] | CT-005 | Dashboard calcula income/expenses de um período | P0 | Positivo | CA-periodo | #3,#4 |
| [ ] | CT-006 | totalBalance soma somente contas ACTIVE | P0 | Positivo | CA-saldo-ativo | #2 |
| [ ] | CT-007 | expenses inclui despesas diretas e parcelas de cartão | P0 | Positivo | CA-despesas | #4 |
| [ ] | CT-008 | Limites de cartão consideram somente cartões ACTIVE | P1 | Positivo | CA-limites | #5 |
| [ ] | CT-009 | expensesByCategory agrega e ordena por total DESC | P1 | Positivo | CA-categorias | #6 |
| [ ] | CT-010 | budgets do período trazem spent | P1 | Positivo | CA-budgets | #7 |
| [ ] | CT-011 | nextInvoice é a primeira fatura OPEN | P1 | Positivo | CA-next-invoice | #8 |
| [ ] | CT-012 | Apenas month completa com o ano corrente | P1 | Borda | CA-month-only | #1 |
| [ ] | CT-013 | Apenas year completa com o mês corrente | P1 | Borda | CA-year-only | #1 |
| [ ] | CT-014 | month=13 retorna 500 INTERNAL_SERVER_ERROR (quirk) | P2 | Negativo | CA-quirk | #9 |
| [ ] | CT-015 | Sem dados no período retorna arrays vazios e nextInvoice null | P2 | Negativo | CA-sem-dados | #2,#3,#8 |

---

### CT-001 - Contrato do response de sucesso do GET /dashboard (sem dados)
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/dashboard/dashboard-response.json` (a criar)
- **Pre-condicoes:** usuário recém-criado (sem dados)
- **Passos:**
  1. Enviar `GET /dashboard` autenticado
  2. Validar o corpo contra o schema estrito
- **Dados de entrada:** sem params
- **Resultado esperado:** HTTP 200; body `DashboardResponse` com
  `expensesByCategory`/`budgets` vazios e `nextInvoice` nulo (`additionalProperties: false`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas;
  [SUPOSICAO] `nextInvoice` nulo (não omitido) — validar o schema com `null` explícito

### CT-002 - Contrato do response de sucesso do GET /dashboard (com dados)
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/dashboard/dashboard-response.json` (a criar)
- **Pre-condicoes:** usuário com conta ACTIVE + despesa direta + cartão + fatura
  OPEN + orçamento no período (via fixtures)
- **Passos:**
  1. Enviar `GET /dashboard` autenticado
  2. Validar o corpo (todos os campos preenchidos) contra o schema
- **Dados de entrada:** sem params
- **Resultado esperado:** HTTP 200; body completo com todos os campos
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-003 - Contrato do response de erro do GET /dashboard
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /dashboard` sem `Authorization`
  2. Validar o corpo de erro (401) contra o schema
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401; body conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-004 - Dashboard sem parâmetros usa mês/ano atuais
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com transação INCOME no mês corrente e outra de 12+
  meses atrás
- **Passos:**
  1. Enviar `GET /dashboard` sem query params
  2. Asserir que `income` reflete apenas o mês corrente
- **Dados de entrada:** nada (params ausentes)
- **Resultado esperado:** HTTP 200; `income` = receitas do mês/ano atuais
- **CA:** CA-sem-periodo - **Regra:** #1
- **Observacoes:** coberto por `DashboardApiTest#deveUsarPeriodoCorrenteQuandoSemParametros`

### CT-005 - Dashboard calcula income/expenses de um período
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com receitas e despesas em dois meses distintos
- **Passos:**
  1. Enviar `GET /dashboard?month=9&year=2026`
  2. Asserir `income` e `expenses` apenas de setembro/2026
- **Dados de entrada:** `month=9&year=2026`
- **Resultado esperado:** HTTP 200; valores do período informado
- **CA:** CA-periodo - **Regra:** #3,#4
- **Observacoes:** coberto por `DashboardApiTest#deveCalcularReceitasEDespesasDoPeriodo`

### CT-006 - totalBalance soma somente contas ACTIVE
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com conta ACTIVE (saldo 1000) e conta INACTIVE
  (saldo 500, via deactivate)
- **Passos:**
  1. Enviar `GET /dashboard`
  2. Asserir `totalBalance`
- **Dados de entrada:** contas via `AccountFixture` + `deactivate`
- **Resultado esperado:** `totalBalance` = saldo das ativas apenas (1000)
- **CA:** CA-saldo-ativo - **Regra:** #2
- **Observacoes:** coberto por `DashboardApiTest#deveSomarSaldoDeContasAtivas`

### CT-007 - expenses inclui despesas diretas e parcelas de cartão
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com despesa direta (via Transaction) + compra
  parcelada com parcela no mês (via Purchase)
- **Passos:**
  1. Enviar `GET /dashboard?month=&year=` do mês
  2. Asserir `expenses` = Σ despesas diretas + Σ parcelas do período
- **Dados de entrada:** transação EXPENSE + compra com parcela no mês
- **Resultado esperado:** `expenses` somados corretamente (pagamento de fatura
  não soma de novo)
- **CA:** CA-despesas - **Regra:** #4
- **Observacoes:** coberto por `DashboardApiTest#deveIncluirParcelasNasDespesas`

### CT-008 - Limites de cartão consideram somente cartões ACTIVE
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com cartão ACTIVE (10k) com compra aberta (2k) e
  cartão INACTIVE (10k)
- **Passos:**
  1. Enviar `GET /dashboard`
  2. Asserir `totalCreditLimit`, `totalUsedLimit`, `totalAvailableLimit`
- **Dados de entrada:** cartões via `CreditCardFixture` + deactivate
- **Resultado esperado:** limite = 10000, usado = 2000, disponível = 8000
  (INACTIVE excluído)
- **CA:** CA-limites - **Regra:** #5
- **Observacoes:** `usedLimit = Σ installments OPEN`

### CT-009 - expensesByCategory agrega e ordena por total DESC
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com despesas em 3 categorias (100, 60, 20) no mês
- **Passos:**
  1. Enviar `GET /dashboard?month=&year=` do mês
  2. Asserir ordem e totais de `expensesByCategory`
- **Dados de entrada:** transações de valores distintos em categorias distintas
- **Resultado esperado:** lista ordenada por `total` DESC (100 → 60 → 20)
- **CA:** CA-categorias - **Regra:** #6
- **Observacoes:** parcelas de cartão também entram na agregação da categoria

### CT-010 - budgets do período trazem spent
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com orçamento + gasto na categoria no mês (via API)
- **Passos:**
  1. Enviar `GET /dashboard?month=&year=` do mês
  2. Asserir `budgets[]` com `amount`, `spent`, `available`
- **Dados de entrada:** budget via `BudgetFixture` + transação EXPENSE
- **Resultado esperado:** `budgets` contém o orçamento do período com `spent` e
  `available` calculados
- **CA:** CA-budgets - **Regra:** #7
- **Observacoes:** mesmo cálculo de `BudgetService.computeSummary`

### CT-011 - nextInvoice é a primeira fatura OPEN
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com 2 faturas OPEN (meses distintos) + 1 fechada
- **Passos:**
  1. Enviar `GET /dashboard`
  2. Asserir `nextInvoice`
- **Dados de entrada:** faturas via compras em meses diferentes
- **Resultado esperado:** `nextInvoice` = fatura OPEN mais antiga
  (`referenceMonth ASC`); fatura fechada não conta
- **CA:** CA-next-invoice - **Regra:** #8
- **Observacoes:** coberto por `DashboardApiTest#deveRetornarPrimeiraFaturaEmAberto`

### CT-012 - Apenas month completa com o ano corrente
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** usuário com receita no mês informado do ano corrente
- **Passos:**
  1. Enviar `GET /dashboard?month=9`
  2. Asserir período efetivo
- **Dados de entrada:** `month=9` apenas
- **Resultado esperado:** dashboard de setembro do **ano corrente**
- **CA:** CA-month-only - **Regra:** #1
- **Observacoes:** [SUPOSICAO] `YearMonth.of(anoCorrente, 9)`

### CT-013 - Apenas year completa com o mês corrente
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** usuário com receita no mês corrente do ano informado
- **Passos:**
  1. Enviar `GET /dashboard?year=2026`
  2. Asserir período efetivo
- **Dados de entrada:** `year=2026` apenas
- **Resultado esperado:** dashboard do **mês corrente** de 2026
- **CA:** CA-year-only - **Regra:** #1
- **Observacoes:** [SUPOSICAO] `YearMonth.of(2026, mêsCorrente)`

### CT-014 - month=13 retorna 500 INTERNAL_SERVER_ERROR (quirk)
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário autenticado
- **Passos:**
  1. Enviar `GET /dashboard?month=13&year=2026`
  2. Asserir o status (bug documentado)
- **Dados de entrada:** `month=13` (e parametrizável: `0`, negativo, ano absurdo)
- **Resultado esperado:** HTTP 500 `INTERNAL_SERVER_ERROR` (`error.unexpected`)
  — sem validação de faixa (`YearMonth.of` lança `DateTimeException`)
- **CA:** CA-quirk - **Regra:** #9
- **Observacoes:** caso executável como **regressão do bug documentado** — não é
  correção de app; registrar em relatório. Diferente de `/budgets?month=13`
  (200) — inconsistência conhecida

### CT-015 - Sem dados no período retorna arrays vazios e nextInvoice null
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário sem dados no período informado
- **Passos:**
  1. Enviar `GET /dashboard?month=1&year=2010` (ano sem dados)
  2. Asserir valores zerados e listas vazias
- **Dados de entrada:** período sem registros
- **Resultado esperado:** `income`/`expenses` = 0, `expensesByCategory`/`budgets`
  vazios, `nextInvoice = null`
- **CA:** CA-sem-dados - **Regra:** #2,#3,#8
- **Observacoes:** `totalBalance` continua com saldo atual (não depende do período)

---