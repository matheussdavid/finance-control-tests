# História: Orçamentos e Reservas

## Descrição

Orçamento mensal por categoria **de despesa** (`EXPENSE`): o usuário define um
valor limite (amount) para uma categoria num mês/ano e o sistema informa quanto
já foi gasto (`spent` = despesas diretas + parcelas de cartão do período) e o
saldo restante (`available = amount − spent`). Orçamento **não bloqueia**
gastos — `available` pode ficar negativo.

Não existe `GET /budgets/{id}` nem `DELETE`. A atualização (`PUT`) altera
**apenas** o `amount`. No frontend, a rota `/reservas` (`SavingsPage`) usa o
mesmo `budgetService` com o rótulo de **"reserva"** e é onde o recurso é
testável por UI.

---

## API

### Endpoints

- **`POST /budgets`** → **201 Created** (`BudgetResponse`)
- **`GET /budgets?month=&year=`** → **200 OK** (`List<BudgetResponse>`; sem
  período → todos, sem paginação)
- **`PUT /budgets/{id}`** → **200 OK** (`BudgetResponse`, só `amount`)
- `GET /budgets/{id}` — **não existe**
- `DELETE /budgets/{id}` — **não existe** (sem remover orçamento)

### Request — `POST /budgets`

```json
{
  "categoryId": "<uuid>",          // Obrigatório, categoria EXPENSE do usuário
  "month": 9,                      // Obrigatório, 1–12 (@Min/@Max)
  "year": 2026,                    // Obrigatório
  "amount": 800.00                 // Obrigatório, DecimalMin 0.01
}
```

### Request — `PUT /budgets/{id}`

```json
{
  "amount": 1000.00                // Obrigatório, DecimalMin 0.01 (único campo)
}
```

### Response — `BudgetResponse` (todos os endpoints)

```json
{
  "id": "<uuid>",
  "categoryId": "<uuid>",
  "categoryName": "Alimentação",
  "month": 9,
  "year": 2026,
  "amount": 800.00,
  "spent": 320.50,
  "available": 479.50,
  "createdAt": "...",
  "updatedAt": "..."
}
```

### Shape de erro (compartilhado)

```json
{
  "timestamp": "...",
  "status": 409,
  "error": "BUSINESS_RULE_VIOLATION",
  "message": "Apenas categorias de despesa podem ter orçamento",
  "path": "...",
  "fields": null
}
```

`fields` só é preenchido em 400 de validação.

### Status usados nesta feature

| Cenário | HTTP | `error` |
|---|---|---|
| Categoria de outra pessoa / inexistente | 404 | `NOT_FOUND` |
| Orçamento inexistente ou de outro usuário (PUT) | 404 | `NOT_FOUND` |
| Categoria não-EXPENSE | 409 | `BUSINESS_RULE_VIOLATION` |
| Orçamento duplicado (user, category, month, year) | 409 | `BUSINESS_RULE_VIOLATION` |
| Campo obrigatório ausente / month fora 1–12 / amount ≤ 0 | 400 | `VALIDATION_ERROR` (+ `fields`) |
| JSON malformado / tipo errado | 400 | `BAD_REQUEST` |
| Sem token / token inválido | 401 | `UNAUTHORIZED` |

---

## Regras de negócio (API)

1. **Categoria deve ser do usuário:** categoria inexistente ou de outro usuário
   → 404 `category.notFound` (ownership via `findByUser_IdAndId`).
2. **Apenas categoria EXPENSE:** categoria `INCOME` → 409
   `category.mustBeExpenseForBudget`.
3. **Unicidade:** `(user, category, month, year)` → 409
   `budget.alreadyExists` (constraint `uq_budget_user_category_month_year`).
4. **`spent`** = Σ despesas diretas (`EXPENSE`) da categoria no período
   (`transaction_date` entre 1º e último dia do mês) + Σ parcelas de cartão da
   categoria no mês da fatura (`installment` por categoria e período).
5. **`available = amount − spent`** — pode negativar; orçamento não bloqueia
   gastos.
6. **Listagem sem período** retorna todos os orçamentos do usuário (sem
   paginação); com `month`/`year` filtra; **`month=13` não validação** — o
   filtro condicional apenas não casa e devolve **lista vazia** (sem erro,
   diferente do quirk de 500 do dashboard).
7. **PUT altera somente `amount`:** demais campos da resposta derivam do
   estado corrente; orçamento inexistente/de outro usuário → 404
   `budget.notFound`.
8. **Sem delete/remover orçamento** (produto não oferece; não existe endpoint).

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** um usuário com uma categoria `EXPENSE`
- **Quando** envio `POST /budgets` com valores únicos e válidos
- **Então** recebo 201 com `BudgetResponse` de `spent` e `available` corretos
- **Dado** um usuário com orçamentos em vários meses
- **Quando** envio `GET /budgets` sem parâmetros
- **Então** recebo a lista completa de orçamentos do usuário
- **Dado** um usuário com orçamentos em meses distintos
- **Quando** envio `GET /budgets?month=9&year=2026`
- **Então** recebo apenas os orçamentos de setembro/2026
- **Dado** um orçamento existente
- **Quando** envio `PUT /budgets/{id}` com novo `amount`
- **Então** recebo o orçamento com `amount`/`available` atualizados
- **Dado** um usuário com despesas diretas e parcelas de cartão na categoria
- **Quando** consulto o orçamento do mês
- **Então** `spent` = Σ despesas diretas + Σ parcelas de cartão do período

### Casos negativos / borda
- **Dado** uma categoria `INCOME`
- **Quando** envio `POST /budgets`
- **Então** recebo 409 `category.mustBeExpenseForBudget`
- **Dado** um orçamento já existente para a combinação
- **Quando** envio `POST /budgets` com a mesma (categoria, mês, ano)
- **Então** recebo 409 `budget.alreadyExists`
- **Dado** `month=0`, `month=13` no create, `amount=0` ou vazio
- **Quando** envio o request
- **Então** recebo 400 `VALIDATION_ERROR` com `fields`
- **Dado** um orçamento de outro usuário
- **Quando** envio `PUT /budgets/{id}`
- **Então** recebo 404 `budget.notFound`
- **Dado** gastos maiores que o orçamento
- **Quando** consulto o orçamento
- **Então** `available` é negativo
- **Dado** `GET /budgets?month=13`
- **Quando** filtro por período
- **Então** recebo 200 com lista vazia (sem validação de faixa)

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/SavingsPage.tsx`, `frontend/src/routes/AppRoutes.tsx`.

### Rotas e telas
- `/reservas` → `SavingsPage` (usa `budgetService` com o rótulo **"reserva"**).
- `BudgetsPage.tsx` (`/budgets`) existe mas **não está roteada** (código
  morto) — orçamentos na UI só são acessíveis por `/reservas`.

### Comportamento (`SavingsPage`)
- Ao abrir `/reservas`, dispara `listBudgets()` + `listCategories()` em
  paralelo.
- Resumo (3 cards): `savings-summary-budgeted` (Σ `amount`),
  `savings-summary-spent` (Σ `spent`), `savings-summary-available` (Σ
  `available`) — considerando reservas com `amount > 0`.
- Modal `savings-modal`: cria reserva pelo POST `/budgets` — categorias do
  select são **exclusivamente EXPENSE ativas**; campos categoria/amount/mês/ano.
  Erro de API exibido em `savings-modal-error`.
- Lista `savings-list-item-{budgetId}`: mostra categoria, `spent / amount`,
  percentual (ring) e texto "restante"/"estourado"; `available < 0` → badge
  "Atenção".
- Últimas reservas → ver planejamento em `savings-list-view-planning`
  (**sem `onClick` — botão morto**).
- Não há edição/remoção de reserva na UI (PUT/DELETE só via API; DELETE não
  existe).

### data-testids relevantes
`savings-page`, `savings-page-new-btn`, `savings-summary-budgeted`,
`savings-summary-spent`, `savings-summary-available`, `savings-modal`,
`savings-modal-title`, `savings-modal-form`, `savings-modal-error`,
`savings-modal-category-select`, `savings-modal-amount-input`,
`savings-modal-month-select`, `savings-modal-year-select`,
`savings-modal-cancel-btn`, `savings-modal-submit-btn`, `savings-list`,
`savings-list-item-{budgetId}`, `savings-list-item-{budgetId}-ring`,
`savings-list-empty-card`, `savings-list-create-first-btn`,
`savings-list-view-planning` (morto).

---

## Regras de negócio (UI)

1. **Reservas são orçamentos:** `/reservas` lista e cria `Budget` via
   `budgetService` com rótulo "reserva".
2. **Só categoria EXPENSE ativa** no select do modal de nova reserva.
3. **Resumo agrega** `budgeted`/`spent`/`available` das reservas ativas
   (`amount > 0`).
4. **Reserva estourada** (`available < 0`) exibe badge "Atenção" e progresso em
   vermelho.
5. **Erro de API** (ex.: 409 duplicado) exibido em `savings-modal-error`; modal
   permanece aberto.
6. **Sem edição/remoção** na UI; `savings-list-view-planning` é botão morto.

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** um usuário com reservas criadas via API
- **Quando** acesso `/reservas`
- **Então** vejo os 3 cards de resumo e a lista de reservas
- **Dado** a tela `/reservas`
- **Quando** abro o modal, preencho categoria EXPENSE + valor + mês/ano e salvo
- **Então** a reserva aparece na lista e o resumo atualiza

### Casos negativos / borda
- **Dado** uma reserva com `spent > amount`
- **Quando** acesso `/reservas`
- **Então** o item exibe badge "Atenção" e valor estourado
- **Dado** uma tentativa de reserva duplicada (categoria/mês/ano iguais)
- **Quando** submeto o modal
- **Então** exibe o erro 409 da API em `savings-modal-error` e não fecha o modal

---

## Referência (código-fonte — app finance-control)

- `backend/src/main/java/br/com/financecontrol/controller/BudgetController.java`
- `backend/src/main/java/br/com/financecontrol/service/BudgetService.java`
- `backend/src/main/java/br/com/financecontrol/dto/budget/BudgetRequest.java` / `BudgetUpdateRequest.java` / `BudgetResponse.java` / `BudgetSummary.java`
- `backend/src/main/resources/messages.properties` (`budget.*`, `category.mustBeExpenseForBudget`)
- `frontend/src/pages/SavingsPage.tsx` (rota `/reservas`)
- `frontend/src/services/budgetService.ts`
- `frontend/src/routes/AppRoutes.tsx` (`/reservas` → SavingsPage; `BudgetsPage` órfã)