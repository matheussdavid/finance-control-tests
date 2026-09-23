# História: Dashboard

## Descrição

O dashboard é o painel financeiro da página inicial (`/`). Consolida, para um
mês/ano, o saldo total das contas ativas, receitas e despesas do período,
limites de cartão, despesas por categoria, orçamentos do mês (com `spent`) e a
próxima fatura em aberto.

Os parâmetros `month`/`year` são **opcionais**: sem ambos → mês/ano atuais; com
apenas um → completa com o corrente. Existe um **quirk conhecido**: `month=13`
(ou ano absurdo) não tem validação de faixa e gera **500 INTERNAL_SERVER_ERROR**
em vez de 400 — bug de produto a ser documentado, não corrigido pela suíte.

---

## API

### Endpoints

- **`GET /dashboard?month=&year=`** → **200 OK** (`DashboardResponse`)
  (autenticação requerida)

### Request — query params (opcionais)

| Param | Tipo | Comportamento |
|---|---|---|
| `month` | `Integer` | 1–12; ausente → mês corrente; `13`/`0`/negativo → **500** (quirk) |
| `year` | `Integer` | ausente → ano corrente; `YearMonth.of(year, month)` lança em valores absurdos |

### Response — `DashboardResponse`

```json
{
  "totalBalance": 1500.00,
  "income": 3000.00,
  "expenses": 820.50,
  "totalCreditLimit": 5000.00,
  "totalUsedLimit": 1200.00,
  "totalAvailableLimit": 3800.00,
  "expensesByCategory": [
    { "categoryId": "<uuid>", "categoryName": "Alimentação", "total": 520.50 }
  ],
  "budgets": [ { "...": "BudgetResponse" } ],
  "nextInvoice": { "...": "InvoiceResponse | null" }
}
```

### Shape de erro (compartilhado)

```json
{
  "timestamp": "...",
  "status": 500,
  "error": "INTERNAL_SERVER_ERROR",
  "message": "Ocorreu um erro inesperado",
  "path": "...",
  "fields": null
}
```

### Status usados nesta feature

| Cenário | HTTP | `error` |
|---|---|---|
| `month` fora de faixa (13, 0, negativo) / ano absurdo | **500** | `INTERNAL_SERVER_ERROR` (quirk, sem validação) |
| Sem token / token inválido | 401 | `UNAUTHORIZED` |

---

## Regras de negócio (API)

1. **Período default:** sem `month`/`year` → mês/ano atuais; com apenas um →
   completa com o atual.
2. **`totalBalance`** = Σ saldo das contas `ACTIVE` (inativas já excluídas).
3. **`income`** = Σ transações `INCOME` no período (1º ao último dia do mês).
4. **`expenses`** = despesas diretas (`EXPENSE`) do período **+ parcelas de
   cartão** do período (`sumCardExpensesByPeriod`). Pagamento de fatura **não**
   é contabilizado de novo (não cria Transaction).
5. **Limites de cartão** (`totalCreditLimit`, `totalUsedLimit`,
   `totalAvailableLimit`) consideram **somente cartões `ACTIVE`**;
   `usedLimit = Σ installments OPEN`; `available = limit − used`.
6. **`expensesByCategory`** agrega despesas diretas + parcelas por categoria e
   ordena por total **DESC** (nome "Desconhecida" se categoria inexistente).
7. **`budgets`** = orçamentos do período com `spent` calculado (`computeSummary`).
8. **`nextInvoice`** = primeira fatura `OPEN` (`referenceMonth ASC`) ou `null`
   se não houver.
9. **Quirk (bug documentado):** `month=13`/`month=0`/ano absurdo → `YearMonth.of`
   lança `DateTimeException` → **500 `INTERNAL_SERVER_ERROR`** via handler
   genérico. Sem validação de faixa nos query params.

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** um usuário com contas, transações, cartões, orçamentos e faturas
- **Quando** envio `GET /dashboard` sem parâmetros
- **Então** recebo o dashboard do mês/ano corrente
- **Dado** um período definido
- **Quando** envio `GET /dashboard?month=9&year=2026`
- **Então** `income`/`expenses` são do período, `totalBalance` de todas as
  contas ativas e `expensesByCategory`/`budgets` do período
- **Dado** conta inativa e cartão inativo
- **Quando** consulto o dashboard
- **Então** `totalBalance`/limites de cartão **excluem** o recurso inativo
- **Dado** gastos em cartão + despesas diretas
- **Quando** consulto o dashboard
- **Então** `expenses` = despesas diretas + parcelas de cartão do período e o
  pagamento da fatura não infla a despesa
- **Dado** uma fatura `OPEN`
- **Quando** consulto o dashboard
- **Então** `nextInvoice` aponta a mais antiga fatura aberta

### Casos negativos / borda
- **Dado** `GET /dashboard?month=13` (ou `month=0`/ano absurdo)
- **Quando** consulto
- **Então** recebo **500 `INTERNAL_SERVER_ERROR`** (bug documentado — sem
  validação de faixa)
- **Dado** sem token
- **Quando** chamo `GET /dashboard`
- **Então** recebo 401 `UNAUTHORIZED`
- **Dado** um usuário sem dados no período
- **Quando** consulto o dashboard
- **Então** `income`/`expenses` zerados, `expensesByCategory`/`budgets` vazios e
  `nextInvoice` `null`

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/DashboardPage.tsx`, `frontend/src/routes/AppRoutes.tsx`.

### Rota
- `/` → `DashboardPage` (`dashboard-page`).

### Comportamento (`DashboardPage`)
- Carrega `GET /dashboard?month=&year=` via `dashboardService` quando muda
  `dashboard-month-select`/`dashboard-year-select`.
- **Métricas hero** (`dashboard-hero-metric-*`): saldo total, "livre para
  gastar" (`totalBalance − totalUsedLimit`) e próxima fatura — com ring de
  utilização de limite.
- **Gastos por categoria** (`dashboard-spending-card`):
  `dashboard-spending-category-{id}` para as top-4 categorias.
- **Próxima fatura** (`dashboard-next-invoice-card`): mostra `nextInvoice` ou
  empty state (`dashboard.noOpenInvoice`).
- **Ações rápidas** (`dashboard-quick-actions-card`):
  `dashboard-quickaction-{expense,income,transfer,savings}` — **todas sem
  `onClick`** (botões mortos).
- **Orçamentos** (`dashboard-budgets-section`): `dashboard-budget-{categoryId}`
  das reservas ativas do mês (máx 3).
- **Empty state** (`dashboard-empty-state`): sem gastos + sem orçamento + sem
  fatura → destaque `dashboard-empty-{first-transaction,create-savings}` — ambos
  **sem `onClick`** (botões mortos).

### data-testids relevantes
`dashboard-page`, `dashboard-page-header`, `dashboard-month-select`,
`dashboard-year-select`, `dashboard-hero-metric-*` (+ `-ring`),
`dashboard-spending-card`, `dashboard-spending-category-{id}`,
`dashboard-next-invoice-card`, `dashboard-quick-actions-card`,
`dashboard-quickaction-{expense,income,transfer,savings}`,
`dashboard-budgets-section`, `dashboard-budget-{categoryId}`,
`dashboard-budgets-view-planning`, `dashboard-budgets-view-more-btn`,
`dashboard-empty-state`, `dashboard-empty-{first-transaction,create-savings}`.

---

## Regras de negócio (UI)

1. **Filtro mês/ano no header** recarrega as métricas via `GET /dashboard`.
2. **Métricas hero** derivam de `totalBalance`, `totalUsedLimit` e
   `nextInvoice`.
3. **Gastos por categoria:** top-4 categorias com maior despesa do período.
4. **Próxima fatura:** primeira fatura `OPEN` (ou empty state).
5. **Botões mortos no dashboard:** `dashboard-quickaction-*`,
   `dashboard-spending-view-all`, `dashboard-budgets-view-planning`,
   `dashboard-budgets-view-more-btn`, `dashboard-empty-first-transaction`,
   `dashboard-empty-create-savings` — **sem `onClick`**; casos de UI de ação
   devem ser `[-]`.
6. **Empty state** aparece quando sem gastos, sem orçamento e sem fatura.
7. **Orçamentos do mês** aparecem em `dashboard-budgets-section`
   (`dashboard-budget-{categoryId}`) com as reservas ativas do período (máx 3).

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** um usuário com dados (transações, orçamentos, fatura) via API
- **Quando** acesso `/`
- **Então** vejo as métricas hero, gastos por categoria, próxima fatura e
  orçamentos do mês
- **Dado** o dashboard carregado
- **Quando** altero `dashboard-month-select`/`dashboard-year-select`
- **Então** as métricas recarregam para o período selecionado

### Casos negativos / borda
- **Dado** um dashboard sem gastos/orçamento/fatura
- **Quando** acesso `/`
- **Então** exibe o empty state de celebração
- **Dado** os botões de ação rápida
- **Quando** clico em qualquer um
- **Então** nada acontece (`[-]` — sem `onClick`)

---

## Referência (código-fonte — app finance-control)

- `backend/src/main/java/br/com/financecontrol/controller/DashboardController.java`
- `backend/src/main/java/br/com/financecontrol/service/DashboardService.java`
- `backend/src/main/java/br/com/financecontrol/dto/dashboard/DashboardResponse.java` / `CategoryExpense.java`
- `backend/src/main/resources/messages.properties` (`error.unexpected`)
- `frontend/src/pages/DashboardPage.tsx` (rota `/`)
- `frontend/src/services/dashboardService.ts`
- `frontend/src/routes/AppRoutes.tsx` (`/` → DashboardPage)