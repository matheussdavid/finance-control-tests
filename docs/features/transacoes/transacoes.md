# História: Transações

## Descrição

Permite registrar receitas (`INCOME`) e despesas (`EXPENSE`), listar com filtros
e paginação e consultar por id. A transação **não é editável nem removível**
(não existem endpoints PUT/PATCH/DELETE). Cada transação referencia conta e
categoria próprias do usuário e movimenta o saldo da conta (INCOME soma,
EXPENSE subtrai). Pagamento de fatura **não** cria transação.

Os filtros de listagem são: `type`, `accountId`, `categoryId`, `startDate`,
`endDate` e paginação (`page`/`size`); a ordenação default é
`transactionDate DESC`.

---

## API

### Endpoints

| Método | Path | Sucesso | Autenticação |
|---|---|---|---|
| POST | `/transactions` | **201 Created** | sim |
| GET | `/transactions` (filtros opcionais) | **200 OK** (`Page<TransactionResponse>`) | sim |
| GET | `/transactions/{id}` | **200 OK** / 404 | sim |
| PUT / PATCH / DELETE | `/transactions/**` | **não existem** (sem edit/remove) | — |

### Request — `POST /transactions`

```json
{
  "type": "EXPENSE",                // INCOME | EXPENSE
  "description": "Supermercado",    // opcional, ≤ 255
  "amount": 150.00,                 // Obrigatório, ≥ 0.01
  "accountId": "<uuid>",
  "categoryId": "<uuid>",
  "transactionDate": "2026-09-10"   // Obrigatório, LocalDate yyyy-MM-dd
}
```

| Campo | Validação JSR-303 | Mensagem |
|---|---|---|
| `type` | `@NotNull` (`TransactionType`: INCOME\|EXPENSE) | type.required |
| `description` | opcional, `@Size(max=255)` | description.tooLong |
| `amount` | `@NotNull`, `@DecimalMin("0.01")` | amount.required / amount.greaterThanZero |
| `accountId` | `@NotNull` | accountId.required |
| `categoryId` | `@NotNull` | categoryId.required |
| `transactionDate` | `@NotNull` (LocalDate) | transactionDate.required |

### Query params — `GET /transactions` (todos opcionais)

`type` (TransactionType), `accountId` (UUID), `categoryId` (UUID),
`startDate`/`endDate` (`yyyy-MM-dd` — `@DateTimeFormat(ISO.DATE)`), `page`,
`size`. Sort default `transactionDate DESC` (`@PageableDefault`). Implementação
em `TransactionRepository.findFiltered`.

### Response — `TransactionResponse` (POST/GET id)

```json
{
  "id": "<uuid>",
  "type": "EXPENSE",
  "description": "Supermercado",
  "amount": 150.00,
  "accountId": "<uuid>",
  "accountName": "Nubank",
  "categoryId": "<uuid>",
  "categoryName": "Alimentação",
  "transactionDate": "2026-09-10",
  "createdAt": "..."
}
```

`GET /transactions` retorna `Page<TransactionResponse>` com metadados Spring
Data (`content/totalElements/totalPages/number/size`).

### Shape de erro (compartilhado)

```json
{
  "timestamp": "...",
  "status": 409,
  "error": "BUSINESS_RULE_VIOLATION",
  "message": "A conta está inativa",
  "path": "...",
  "fields": null
}
```

`fields` (map campo→mensagem) só é preenchido em 400 de validação. Erros já
chegam traduzidos para pt-BR (`messages.properties`).

### Status usados nesta feature

| Cenário | HTTP | `error` |
|---|---|---|
| Criação/leitura com dados válidos | 201 / 200 | — |
| Transação inexistente ou de outro usuário (ownership) | 404 | `NOT_FOUND` |
| Conta inativa / categoria inativa / tipo incompatível | 409 | `BUSINESS_RULE_VIOLATION` |
| Campo obrigatório ausente / `amount` < 0.01 | 400 | `VALIDATION_ERROR` (+ `fields`) |
| `type` inválido, filtro `type=TRANSFER` ou data malformada | 400 | `BAD_REQUEST` |

---

## Regras de negócio (API)

1. **INCOME soma / EXPENSE subtrai o saldo** da conta
   (`TransactionService.java:104-109`); saldo pode negativar (sem validação).
2. **Conta inativa → 409:** `account.notActive` (`TransactionService.java:90-92`).
3. **Categoria inativa → 409:** `category.notActive` (`TransactionService.java:93-95`).
4. **Tipo incompatível → 409:** INCOME exige categoria `INCOME`
   (`category.mustBeIncome`); EXPENSE exige `EXPENSE` (`category.mustBeExpense`)
   (`TransactionService.java:96-101`).
5. **Sem edit/remove:** não existem endpoints PUT/PATCH/DELETE para transação.
6. **Ownership:** conta/categoria são carregadas por `findByUser_IdAndId`
   (`account.notFound`/`category.notFound` → 404); leitura por `id` também
   (`transaction.notFound` → 404) (`TransactionService.java:47-51,83-87`).
7. **Pagamento de fatura NÃO cria Transaction** — o `pay` apenas debita o saldo
   (`InvoiceService.java:85-94`).
8. **Lista ordenada por `transactionDate DESC`** com filtros opcionais e
   paginação Spring Data (`TransactionRepository.findFiltered`).
9. **Sem validação de data futura no backend** — `@DateTimeFormat` só exige
   `yyyy-MM-dd`; o front limita `max=today()` no QuickCapture.

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** um usuário com conta e categoria EXPENSE ativas
- **Quando** envio `POST /transactions` com `type=EXPENSE`, conta/categoria
  compatíveis e `amount` válido
- **Então** recebo 201 com `TransactionResponse` e o `balance` da conta é
  **subtraído** do valor

- **Dado** um usuário com conta e categoria INCOME ativas
- **Quando** envio `POST /transactions` com `type=INCOME` e `amount` válido
- **Então** recebo 201 e o `balance` da conta é **somado** do valor

- **Dado** uma despesa criada via API
- **Quando** consulto o PostgreSQL pela `description` da transação
- **Então** o registro existe com `type`, `amount`, `transactionDate`,
  `accountName` e `categoryName` corretos (persistência real)

- **Dado** transações criadas
- **Quando** envio `GET /transactions` com filtros (`type`, `accountId`,
  `categoryId`, `startDate`, `endDate`)
- **Então** recebo 200 com `Page` apenas das transações que casam, ordenada por
  `transactionDate DESC`

- **Dado** uma transação existente
- **Quando** envio `GET /transactions/{id}`
- **Então** recebo 200 com os dados da transação

### Casos negativos / borda
- **Dado** uma conta inativa
- **Quando** envio `POST /transactions` para essa conta
- **Então** recebo 409 (`account.notActive`)

- **Dado** uma categoria inativa
- **Quando** envio `POST /transactions` com essa categoria
- **Então** recebo 409 (`category.notActive`)

- **Dado** `type=INCOME` com categoria EXPENSE
- **Quando** envio a transação
- **Então** recebo 409 (`category.mustBeIncome`)

- **Dado** `type=EXPENSE` com categoria INCOME
- **Quando** envio a transação
- **Então** recebo 409 (`category.mustBeExpense`)

- **Dado** um `POST /transactions` com campo obrigatório ausente
  (`type`/`amount`/`accountId`/`categoryId`/`transactionDate`) ou `amount` < 0.01
- **Quando** envio a transação
- **Então** recebo 400 `VALIDATION_ERROR` com `fields` apontando o campo

- **Dado** conta ou categoria de outro usuário, ou transação inexistente
- **Quando** envio a requisição
- **Então** recebo 404 `NOT_FOUND`

- **Dado** `type` inválido no corpo, filtro `type=TRANSFER`, ou `startDate`
  fora do formato `yyyy-MM-dd`
- **Quando** envio a requisição
- **Então** recebo 400 `BAD_REQUEST`

> "Pagamento de fatura não cria Transaction" (regra 7) e limites de edição
> (regra 5): documentados como casos `[-]`/nota — exigem setup de
> cartão/compra/fatura (features cartões/faturas, fora desta bateria) ou são
> ausência de endpoint.

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/SpendPage.tsx`, `frontend/src/components/QuickCapture.tsx`, `frontend/src/services/transactionService.ts`, `frontend/src/i18n/pt.ts:110-120,389-399`.

### Rota
- `/gastos` (`spend-page`); acesso pela navbar.

### Quick Add Bar (`spend-page-quickadd`)
- Botões `spend-page-quickadd-expense`, `-expense-type`, `-income-type`,
  `-transfer-type` apenas fazem `setQuickCaptureOpen(true)`
  (`SpendPage.tsx:94-129`) — **não pré-selecionam o tipo**; o modal abre sempre
  em modo `expense` (bug de UX, `QuickCapture.tsx:58`).

### Filtros (`spend-page-filters-panel`, aberto por `spend-page-filters-toggle`)
- `spend-page-filter-type` (Todas/Receita/Despesa/**Transferência**),
  `spend-page-filter-account`, `spend-page-filter-category`,
  `spend-page-filter-start`, `spend-page-filter-end`; `spend-page-filters-clear`
  limpa. Sempre zera `page=0`.
- **Bug conhecido:** selecionar `TRANSFER` no filtro de tipo envia
  `type=TRANSFER`, que o backend rejeita → **400 BAD_REQUEST** exibido no
  `Message` (o select oferece a opção, `SpendPage.tsx:166`).

### Feed (`spend-page-feed-card`)
- Lista `SpendFeed` com as transações da página (grupo por data);
  `spend-page-pagination-prev`/`-next` quando `totalPages > 1`.

### QuickCapture (modal)
- Abas `quickcapture-tab-{expense,income,transfer}`; form
  `quickcapture-form` com `quickcapture-amount-input` (`min=0.01`, `required`),
  `quickcapture-description-input` (`maxLength=255`, opcional),
  `quickcapture-account-select` (somente contas ACTIVE),
  `quickcapture-category-select` (somente categorias ACTIVE do tipo do modo),
  `quickcapture-date-input` (`required`, `max=today()`), `quickcapture-cancel-btn`,
  `quickcapture-submit-btn`. Erro exibido em `quickcapture-error`.
- Sucesso recarrega feed/contas; contas/categorias inativas **não** aparecem nos
  selects (filtro de `ACTIVE`), portanto erros 409 são difíceis de atingir via UI.

### data-testids relevantes
`spend-page`, `spend-page-filters-toggle`, `spend-page-new-btn`,
`spend-page-quickadd-{expense,expense-type,income-type,transfer-type}`,
`spend-page-filters-panel`, `spend-page-filter-{type,account,category,start,end}`,
`spend-page-filters-clear`, `spend-page-feed-card`,
`spend-page-pagination-{prev,next}`, `quickcapture-modal`,
`quickcapture-tab-{expense,income,transfer}`, `quickcapture-form`,
`quickcapture-amount-input`, `quickcapture-description-input`,
`quickcapture-account-select`, `quickcapture-category-select`,
`quickcapture-date-input`, `quickcapture-cancel-btn`,
`quickcapture-submit-btn`, `quickcapture-error`.

---

## Regras de negócio (UI)

1. **Modal QuickCapture** cria transação (aba expense/income) ou transferência
   (aba transfer); campos das abas têm `required`/`min=0.01`/`maxLength=255`.
2. **Selects restritos:** contas ACTIVE e categorias ACTIVE (e do tipo do modo)
   — recurso inativo não é selecionável.
3. **Data limitada:** `quickcapture-date-input` tem `max={today()}` (front só)
   — backend não valida data futura.
4. **Filtro `TRANSFER` é bug conhecido** (selecionável mas causa 400 na API).
5. **Quick add do /gastos não pré-seleciona o tipo**; modal sempre abre em
   `expense` (bug de UX).
6. **Sucesso recarrega feed e contas**; erro da API exibido em
   `quickcapture-error` (no modal) ou `Message` (na página).
7. **Página sem form de edição:** o /gastos não oferece editar/remover
   lançamento (consistente com backend sem PUT/DELETE).

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** a tela `/gastos` com usuário autenticado (conta e categoria EXPENSE
  via API)
- **Quando** abro o QuickCapture, preencho valor/descrição/conta/categoria/data
  e submeto
- **Então** o modal fecha, a transação aparece em `spend-page-feed-card` e o
  saldo da conta é atualizado

- **Dado** a aba income do QuickCapture
- **Quando** preencho uma receita (categoria INCOME) e submeto
- **Então** a transação INCOME aparece no feed

- **Dado** o painel de filtros aberto
- **Quando** seleciono tipo/conta/categoria/período e aplico
- **Então** o feed mostra apenas as transações que casam, do mais recente ao
  mais antigo

### Casos negativos / borda
- **Dado** o QuickCapture com campos obrigatórios vazios
- **Quando** tento submeter
- **Então** a validação `required` bloqueia e não chama a API

- **Dado** o filtro de tipo selecionado como `TRANSFER`
- **Quando** aplico o filtro
- **Então** o `Message` exibe o erro 400 da API (bug conhecido da opção)

- **Dado** o campo data do QuickCapture
- **Quando** tento informar data futura
- **Então** `max={today()}` impede a seleção

- **Dado** o botão de quickadd de receita/transferência
- **Quando** clico em `spend-page-quickadd-income-type`
- **Então** o modal abre em modo `expense` (não pré-seleciona o tipo — bug de UX)

---

## Referência (código-fonte — app finance-control)

- `backend/src/main/java/br/com/financecontrol/controller/TransactionController.java:41-65`
- `backend/src/main/java/br/com/financecontrol/service/TransactionService.java:46-109`
- `backend/src/main/java/br/com/financecontrol/dto/transaction/TransactionRequest.java:13-36` / `TransactionResponse.java:10-21`
- `backend/src/main/java/br/com/financecontrol/entity/enums/TransactionType.java`
- `backend/src/main/resources/messages.properties:26-30,44,55,63-69,75,80`
- `frontend/src/pages/SpendPage.tsx:18-120,133-277`
- `frontend/src/components/QuickCapture.tsx:57-132,195-428`
- `frontend/src/services/transactionService.ts:23-36`
- `frontend/src/i18n/pt.ts:110-120,389-399`