# Casos de Teste UI - Planejamento

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/planejamento/planejamento.md

## Resumo Executivo
- **Total de casos de teste:** 7
- **Executados:** 0/7 (0%)
- **Distribuição por prioridade:**
  - P0: 2
  - P1: 3
  - P2: 2
- **Critérios de aceite cobertos:** 4 de 4 (UI)
- **Regras de negócio cobertas:** 5/6 (UI) — regra 6 (estado depende de massa
  de API) não tem CT dedicado; é pré-condição transversal de todos os CTs
- **Observações gerais:** feature declarada como **fase E2E posterior** —
  todos os casos exigem estado de API prévio (transações, compras, faturas,
  orçamentos) antes de navegar para `/planejamento`. Não há endpoint próprio;
  a view compõe `/dashboard`, `/budgets` e `/invoices`. Os casos são de
  integração/visual.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Tela /planejamento exibe overview orçado, gasto e faturas | P0 | Positivo | CA-exibir-planejamento | #2,#3 |
| [ ] | CT-002 | Projeção income/expense/balance reflete o dashboard do período | P0 | Positivo | CA-projecao | #4 |
| [ ] | CT-003 | Orçamentos do mês aparecem no card com progresso | P1 | Positivo | CA-exibir-planejamento | #2 |
| [ ] | CT-004 | Faturas OPEN aparecem no card com badges | P1 | Positivo | CA-exibir-planejamento | #3 |
| [ ] | CT-005 | Alterar mês/ano refaz as três chamadas | P1 | Borda | CA-filtro | #5 |
| [ ] | CT-006 | Sem dados no período exibe empty states e projeção zerada | P2 | Borda | CA-empty | #1,#4 |
| [ ] | CT-007 | Orçamento estourado exibe "estourado" no item | P2 | Negativo | CA-estouro | #2 |

---

### CT-001 - Tela /planejamento exibe overview orçado, gasto e faturas
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com orçamento (→ R$ 800) + fatura OPEN (→ R$ 400)
  + gasto parcial no mês (tudo via API), autenticado
- **Passos:**
  1. Navegar para `/planejamento`
  2. Verificar `planning-overview-budgeted` = Σ amount
  3. Verificar `planning-overview-spent` = Σ spent
  4. Verificar `planning-overview-invoices` = Σ totalAmount das faturas OPEN
- **Dados de entrada:** budget via `BudgetFixture` + fatura via `PurchaseFixture` + transação EXPENSE
- **Resultado esperado:** overview com os três valores conforme massa
- **CA:** CA-exibir-planejamento - **Regra:** #2,#3
- **Observacoes:** coberto por `PlanningWebTest#deveExibirOverviewDoPlanejamento`

### CT-002 - Projeção income/expense/balance reflete o dashboard do período
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com receita e despesa no mês (via API)
- **Passos:**
  1. Navegar para `/planejamento`
  2. Verificar `planning-projection-income` = dashboard.income
  3. Verificar `planning-projection-expense` = dashboard.expenses
  4. Verificar `planning-projection-balance` = income − expenses
- **Dados de entrada:** transações INCOME/EXPENSE no mês
- **Resultado esperado:** projeção correta (income − expense >= 0 → verde)
- **CA:** CA-projecao - **Regra:** #4
- **Observacoes:** coberto por `PlanningWebTest#deveExibirProjecaoDoMes`

### CT-003 - Orçamentos do mês aparecem no card com progresso
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com orçamento ativo no mês (via API)
- **Passos:**
  1. Navegar para `/planejamento`
  2. Verificar `planning-budget-{categoryId}` no card
  3. Verificar percentual/spent/amount exibidos
- **Dados de entrada:** budget via `BudgetFixture`
- **Resultado esperado:** card de orçamento com itens do mês selecionado (dos
  `budgets` do `/dashboard` + listBudgets)
- **CA:** CA-exibir-planejamento - **Regra:** #2
- **Observacoes:** orçamento de outro mês NÃO deve aparecer (filtro client por
  `month`/`year`)

### CT-004 - Faturas OPEN aparecem no card com badges
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com fatura OPEN (via compra) 
- **Passos:**
  1. Navegar para `/planejamento`
  2. Verificar `planning-invoice-{id}` no card
  3. Verificar valor, badge de status e datas (due/closing/reference)
- **Dados de entrada:** fatura via `PurchaseFixture`
- **Resultado esperado:** fatura aberta listada com badge `OPEN` e datas relativas
- **CA:** CA-exibir-planejamento - **Regra:** #3
- **Observacoes:** só faturas `OPEN` aparecem no card

### CT-005 - Alterar mês/ano refaz as três chamadas
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário com dados em dois meses distintos (via API)
- **Passos:**
  1. Navegar para `/planejamento`
  2. Alterar `planning-month-select` (ou `planning-year-select`)
  3. Verificar cards/projeção atualizados para o novo período
- **Dados de entrada:** meses com valores distintos
- **Resultado esperado:** nova chamada a `/dashboard` + `/budgets` +
  `/invoices` com o novo período
- **CA:** CA-filtro - **Regra:** #5
- **Observacoes:** coberto por `PlanningWebTest#deveAtualizarAoMudarPeriodo`

### CT-006 - Sem dados no período exibe empty states e projeção zerada
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário sem dados no período selecionado
- **Passos:**
  1. Navegar para `/planejamento`
  2. Selecionar período sem registros
  3. Verificar empty states nos cards de orçamento e fatura e projeção zerada
- **Dados de entrada:** período sem dados
- **Resultado esperado:** `planning-budgets-card` e `planning-invoices-card` com
  EmptyState; projeção com 0/0/0
- **CA:** CA-empty - **Regra:** #1,#4
- **Observacoes:** [NA] valida condição visual de "Nenhuma fatura em aberto" e
  "sem orçamentos"

### CT-007 - Orçamento estourado exibe "estourado" no item
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** usuário com orçamento R$ 200 e gasto R$ 300 na categoria
  (via API)
- **Passos:**
  1. Navegar para `/planejamento`
  2. Verificar `planning-budget-{categoryId}` com texto `planning.exceeded`
  3. Verificar progresso/ring vermelho
- **Dados de entrada:** orçamento + transação EXPENSE que estoura
- **Resultado esperado:** item mostra estourado e percentual > 100% com cor de
  despesa
- **CA:** CA-estouro - **Regra:** #2
- **Observacoes:** mesmo comportamento de `SavingsPage` estourada (badge); aqui
  texto "estourado"

---