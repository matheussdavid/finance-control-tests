# Casos de Teste UI - Dashboard

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/dashboard/dashboard.md

## Resumo Executivo
- **Total de casos de teste:** 10
- **Executados:** 0/10 (0%)
- **Distribuição por prioridade:**
  - P0: 2
  - P1: 4
  - P2: 4
- **Critérios de aceite cobertos:** 4 de 4 (UI)
- **Regras de negócio cobertas:** 6/7 (UI) — regra 5 (botões mortos) coberta
  apenas por CT-007/008/009/010 `[-]`; demais por CTs `[ ]`
- **Observações gerais:** todos os CTs dependem de massa criada por API antes
  de abrir `/` (transações, gastos por categoria, orçamento, fatura). 4 casos
  são `[-]` pois os botões **não têm `onClick`** (bug de produto documentado) e
  o empty state reflete estado pré-condição. Revisão: agrupados os 4 botões
  mortos de ações rápidas em 1 caso `[-]` (CT-009).

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Dashboard exibe métricas, gastos, fatura e orçamentos do mês | P0 | Positivo | CA-exibir-dashboard | #2,#3,#4,#7 |
| [ ] | CT-002 | Alterar mês/ano recarrega as métricas | P0 | Positivo | CA-filtro | #1 |
| [ ] | CT-003 | Alterar year isolado mantém mês atual | P1 | Borda | CA-filtro | #1 |
| [ ] | CT-004 | Métrica de disponibilidade reflete limite pós-compras | P1 | Positivo | CA-exibir-dashboard | #2 |
| [ ] | CT-005 | Empty state aparece sem gastos/orçamento/fatura | P1 | Borda | CA-empty | #6 |
| [ ] | CT-006 | Lista de orçamentos do dashboard reflete reservas do mês | P1 | Positivo | CA-exibir-dashboard | #7 |
| [-] | CT-007 | Botão view all de gastos por categoria sem ação | P2 | Borda | - | #5 |
| [-] | CT-008 | Botão ver planejamento completo sem ação (budgets) | P2 | Borda | - | #5 |
| [-] | CT-009 | Ações rápidas (expense/income/transfer/savings) sem ação | P2 | Borda | - | #5 |
| [-] | CT-010 | Botões do empty state sem ação | P2 | Borda | - | #5 |

---

### CT-001 - Dashboard exibe métricas, gastos, fatura e orçamentos do mês
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com conta ACTIVE, despesa direta e compra parcelada
  (fatura OPEN), orçamento no mês (tudo via API)
- **Passos:**
  1. Navegar para `/` (autenticado)
  2. Verificar `dashboard-hero-metric-*` (3 cards)
  3. Verificar `dashboard-spending-category-{id}` da categoria com gasto
  4. Verificar `dashboard-next-invoice-card` com a fatura
  5. Verificar `dashboard-budget-{categoryId}` do orçamento
- **Dados de entrada:** massa via fixtures (conta, transação, compra, orçamento)
- **Resultado esperado:** métricas/fatura/orçamentos refletem os dados do mês
- **CA:** CA-exibir-dashboard - **Regra:** #2,#3,#4
- **Observacoes:** coberto por `DashboardWebTest#deveExibirResumoDoDashboard`

### CT-002 - Alterar mês/ano recarrega as métricas
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com gastos distintos em 2 meses (via API)
- **Passos:**
  1. Abrir `/`
  2. Selecionar mês com dados em `dashboard-month-select`
  3. Verificar valor da métrica alterado
  4. Selecionar ano em `dashboard-year-select` e repetir
- **Dados de entrada:** meses com valores distintos
- **Resultado esperado:** métricas recarregam conforme período; nova chamada a
  `/dashboard`
- **CA:** CA-filtro - **Regra:** #1
- **Observacoes:** coberto por `DashboardWebTest#deveRecarregarAoMudarPeriodo`

### CT-003 - Alterar year isolado mantém mês atual
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado
- **Passos:**
  1. Abrir `/`
  2. Alterar apenas `dashboard-year-select`
  3. Verificar chamada de recarga
- **Dados de entrada:** novo ano
- **Resultado esperado:** dashboard recarrega com mês atual preservado
- **CA:** CA-filtro - **Regra:** #1
- **Observacoes:** [SUPOSICAO] o estado de mês/ano é independente no componente

### CT-004 - Métrica de disponibilidade reflete limite pós-compras
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com conta (10000) e cartão com compra aberta (2000)
- **Passos:**
  1. Abrir `/`
  2. Verificar a métrica "livre para gastar" (`dashboard-hero-metric-*`)
- **Dados de entrada:** massa via API
- **Resultado esperado:** valor ≈ `totalBalance − totalUsedLimit` (8000 − gastos)
- **CA:** CA-exibir-dashboard - **Regra:** #5
- **Observacoes:** [SUPOSICAO] `freeToSpend = balance − usedLimit`

### CT-005 - Empty state aparece sem gastos/orçamento/fatura
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário recém-criado (sem dados no mês)
- **Passos:**
  1. Abrir `/`
  2. Verificar `dashboard-empty-state`
- **Dados de entrada:** - 
- **Resultado esperado:** empty state visível com `dashboard-empty-state`
- **CA:** CA-empty - **Regra:** #6
- **Observacoes:** coberto por `DashboardWebTest#deveExibirEmptyState`

### CT-006 - Lista de orçamentos do dashboard reflete reservas do mês
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com orçamento ativo no mês corrente (via API)
- **Passos:**
  1. Abrir `/`
  2. Verificar `dashboard-budgets-section` e `dashboard-budget-{categoryId}`
- **Dados de entrada:** budget via `BudgetFixture`
- **Resultado esperado:** orçamento do mês listado com spent/available
- **CA:** CA-exibir-dashboard - **Regra:** #7
- **Observacoes:** max de 3 cards exibidos (budgets ativos do mês)

### CT-007 - Botão view all de gastos por categoria sem ação
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário com +4 categorias com gasto no mês (para renderizar
  `dashboard-spending-view-all`)
- **Passos:**
  1. Abrir `/`
  2. Clicar em `dashboard-spending-view-all`
- **Dados de entrada:** 4+ categorias com gasto
- **Resultado esperado:** [NA] botão sem `onClick` (DashboardPage.tsx:220-227) —
  nenhuma ação
- **CA:** - **Regra:** #5
- **Observacoes:** [-] botão morto; condição de render exige +4 categorias

### CT-008 - Botão ver planejamento completo sem ação (budgets)
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário com orçamento no mês
- **Passos:**
  1. Abrir `/`
  2. Clicar em `dashboard-budgets-view-planning` e
     `dashboard-budgets-view-more-btn` (se >3 reservas)
- **Dados de entrada:** - 
- **Resultado esperado:** [NA] ambos sem `onClick` (DashboardPage.tsx:384-391,
  439-449)
- **CA:** - **Regra:** #5
- **Observacoes:** [-] botões mortos

### CT-009 - Ações rápidas (expense/income/transfer/savings) sem ação
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado
- **Passos:**
  1. Abrir `/`
  2. Clicar em cada `dashboard-quickaction-{expense,income,transfer,savings}`
  3. Verificar ausência de navegação/efeito
- **Dados de entrada:** - 
- **Resultado esperado:** [NA] todos sem `onClick` (DashboardPage.tsx:322-373)
- **CA:** - **Regra:** #5
- **Observacoes:** [-] botões mortos; 4 variações no mesmo caso `[-]`

### CT-010 - Botões do empty state sem ação
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário sem dados (empty state visível)
- **Passos:**
  1. Abrir `/`
  2. Clicar em `dashboard-empty-first-transaction` e
     `dashboard-empty-create-savings`
- **Dados de entrada:** - 
- **Resultado esperado:** [NA] ambos sem `onClick` (DashboardPage.tsx:466-477)
- **CA:** - **Regra:** #5
- **Observacoes:** [-] botões mortos

---