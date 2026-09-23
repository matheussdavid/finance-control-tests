# História: Planejamento

## Descrição

Página de projeção do mês (`/planejamento`). **Não existe módulo backend
próprio** — a view compõe três endpoints já cobertos por outras features:
`/dashboard` (receitas/despesas/saldo), `/budgets` (orçamentos) e `/invoices`
(faturas). O frontend dispara as três chamadas em paralelo para o mês/ano
selecionado e projeta: total orçado, gasto realizado, faturas em aberto e

projeção saldo = receitas − despesas.

**Automação declarada como fase E2E posterior:** não há regra de negócio ou
endpoint próprio para testar na camada de contrato. Os casos são de
**integração/visual**: criar estado via API das features anteriores
(transações, compras, faturas, orçamentos) e validar que a view composta gera
as projeções corretas. API desta feature = `dashboard` + `budgets` +
`invoices` (ver arquivos `dashboard-api.md`, `orcamentos-reservas-api.md`,
`faturas-api.md`).

---

## API

### Endpoints próprios

**Nenhum.** O backend não possui controller/service `/planning`.

| Endpoint utilizado pela UI | Dono (feature) | Arquivo de testes |
|---|---|---|
| `GET /dashboard?month=&year=` | dashboard | `dashboard-api.md` |
| `GET /budgets` (+filtro client) | orcamentos-reservas | `orcamentos-reservas-api.md` |
| `GET /invoices` | faturas | `faturas-api.md` |

Regras de negócio, DTOs e contratos dos três endpoints estão documentados nas
features de origem — **não duplicar aqui**.

---

## Regras de negócio (API)

1. **Sem endpoint próprio:** `/planejamento` é view composta; qualquer teste de
   API a esta página é coberto pelas features dashboard/orçamentos/faturas.

---

## Critérios de aceite (API)

Nenhum — o comportamento de API do planejamento é o somatório dos contratos das
3 features de origem. Casos `[-]` no arquivo `planejamento-api.md` apenas
registram essa composição como referência (para rastreabilidade).

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/PlanningPage.tsx`, `frontend/src/routes/AppRoutes.tsx`.

### Rota
- `/planejamento` → `PlanningPage` (`planning-page`).

### Comportamento (`PlanningPage`)
- Ao abrir, dispara em paralelo `getDashboard(month, year)` +
  `listBudgets()` + `listInvoices(0, 10)` e refaz as 3 chamadas quando mudam
  `planning-month-select`/`planning-year-select`.
- **Overview** (`planning-overview-*`):
  - `planning-overview-budgeted` = Σ `amount` dos budgets ativos do mês.
  - `planning-overview-spent` = Σ `spent` dos budgets ativos do mês.
  - `planning-overview-invoices` = Σ `totalAmount` das faturas `OPEN`.
- **Orçamentos** (`planning-budgets-card`): item `planning-budget-{categoryId}`
  por orçamento ativo do mês (progresso + "remanescente"/"estourado").
- **Faturas** (`planning-invoices-card`): item `planning-invoice-{id}` por
  fatura `OPEN` (valor, badges, vencimento/fechamento/referência).
- **Projeção** (`planning-projection-*`): `income` e `expenses` do `/dashboard`
  do período; `balance` = `income − expenses`.

### data-testids relevantes
`planning-page`, `planning-page-header`, `planning-month-select`,
`planning-year-select`, `planning-overview-budgeted`,
`planning-overview-spent`, `planning-overview-invoices`,
`planning-budgets-card`, `planning-budget-{categoryId}`,
`planning-invoices-card`, `planning-invoice-{id}`,
`planning-projection-income`, `planning-projection-expense`,
`planning-projection-balance`.

---

## Regras de negócio (UI)

1. **View composta:** a página agrega `/dashboard`, `/budgets` e `/invoices`
   do período.
2. **Overview orçado/gasto:** Σ `amount` / Σ `spent` dos budgets ativos do
   período (`month`/`year` selecionados).
3. **Faturas em aberto:** Σ `totalAmount` de faturas `OPEN`; badges de status.
4. **Projeção:** `income` e `expenses` vêm do dashboard do período;
   `balance = income − expenses`.
5. **Filtro mês/ano** nas 3 chamadas ao trocar o período.
6. **Estado visual depende de massa de API** — criação de transações/compras/
   faturas/orçamentos deve preceder a navegação.

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** um usuário com orçamentos, faturas e transações no mês (via API)
- **Quando** acesso `/planejamento`
- **Então** vejo overview (orçado/gasto/faturas), cards de orçamento e fatura e
  a projeção income/expense/balance
- **Dado** o planejamento carregado
- **Quando** altero `planning-month-select`
- **Então** os cards e a projeção refletem o novo período

### Casos negativos / borda
- **Dado** um usuário sem dados no período
- **Quando** acesso `/planejamento`
- **Então** os cards exibem empty states e projeção zerada
- **Dado** orçamento estourado no mês
- **Quando** acesso `/planejamento`
- **Então** o item exibe "estourado" (`planning.exceeded`)

---

## Referência (código-fonte — app finance-control)

- `frontend/src/pages/PlanningPage.tsx` (rota `/planejamento`)
- `frontend/src/services/dashboardService.ts` / `budgetService.ts` / `invoiceService.ts`
- `frontend/src/routes/AppRoutes.tsx` (`/planejamento` → PlanningPage)
- Backend: **sem módulo próprio** — ver `DashboardController.java`, `BudgetController.java`, `InvoiceController.java`