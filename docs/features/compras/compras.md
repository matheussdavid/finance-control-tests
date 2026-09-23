# História: Compras (cartão parcelado)

## Descrição

Registra compras no cartão de crédito com parcelamento: criar (`POST /purchases`),
listar paginada (`GET /purchases`, sort `purchaseDate DESC`) e buscar por id
(`GET /purchases/{id}`). **Sem update/delete e sem filtros** além da paginação.

Tudo acontece numa transação única: valida cartão/categoria/limite, divide o
`totalAmount` em parcelas (`RoundingMode.DOWN`, resto na última), cria (ou
reaproveita) as faturas `OPEN` do período e grava as parcelas `OPEN` — que já
comprometem o `usedLimit` do cartão. A fatura é **criada automaticamente**;
não existe endpoint de criação manual.

---

## API

### Endpoints

- **`POST /purchases`** → **201 Created** (autenticação requerida: **sim**)
- **`GET /purchases`** → **200 OK** (`Page<PurchaseResponse>`, sort `purchaseDate DESC`)
- **`GET /purchases/{id}`** → **200 OK** / **404**
- `PUT/DELETE /purchases/{id}` — **não existem**

### Request — `POST /purchases`

```json
{
  "creditCardId": "<uuid>",       // Obrigatório (@NotNull)
  "categoryId": "<uuid>",         // Obrigatório (@NotNull), deve ser EXPENSE e ACTIVE
  "description": "Notebook",      // Opcional, ≤ 255
  "totalAmount": 100.00,          // Obrigatório, DecimalMin 0.01
  "installmentsCount": 3,         // Obrigatório, Min 1
  "purchaseDate": "2026-09-05"    // Obrigatório (LocalDate yyyy-MM-dd)
}
```

Validação JSR-303: `creditCardId.required`, `categoryId.required`,
`description.tooLong`, `totalAmount.required`,
`totalAmount.greaterThanZero`, `installmentsCount.required`,
`installmentsCount.atLeastOne`, `purchaseDate.required`.

### Response — `PurchaseResponse` (201 e 200)

```json
{
  "id": "<uuid>",
  "creditCardId": "<uuid>",
  "creditCardName": "Visa",
  "categoryId": "<uuid>",
  "categoryName": "Eletrônicos",
  "description": "Notebook",
  "totalAmount": 100.00,
  "installmentsCount": 3,
  "purchaseDate": "2026-09-05",
  "createdAt": "2026-09-05T10:30:00",
  "installments": [
    { "id": "<uuid>", "invoiceId": "<uuid>", "referenceMonth": "2026-09-01",
      "number": 1, "amount": 33.33, "status": "OPEN" },
    { "id": "<uuid>", "invoiceId": "<uuid>", "referenceMonth": "2026-10-01",
      "number": 2, "amount": 33.33, "status": "OPEN" },
    { "id": "<uuid>", "invoiceId": "<uuid>", "referenceMonth": "2026-11-01",
      "number": 3, "amount": 33.34, "status": "OPEN" }
  ]
}
```

`GET /purchases` envolve o mesmo shape num `Page` do Spring Data
(`content/totalElements/totalPages/number/size`).

### Shape de erro (compartilhado)

```json
{
  "timestamp": "...",
  "status": 409,
  "error": "BUSINESS_RULE_VIOLATION",
  "message": "...",
  "path": "...",
  "fields": null
}
```

### Status usados nesta feature

| Cenário | HTTP | `error` |
|---|---|---|
| Compra criada | 201 | — |
| Listagem / busca por id | 200 | — |
| Campos ausentes / `totalAmount` < 0.01 / `installmentsCount` < 1 / `description` > 255 | 400 | `VALIDATION_ERROR` (+ `fields`) |
| `purchaseDate` malformada / JSON inválido | 400 | `BAD_REQUEST` |
| Cartão/categoria inexistente ou de outro usuário; purchase id inexistente | 404 | `NOT_FOUND` |
| Cartão INACTIVE | 409 | `BUSINESS_RULE_VIOLATION` (`creditCard.notActive`) |
| Categoria INACTIVE | 409 | `BUSINESS_RULE_VIOLATION` (`category.notActive`) |
| Categoria não-EXPENSE | 409 | `BUSINESS_RULE_VIOLATION` (`category.mustBeExpenseForPurchase`) |
| `totalAmount` > limite disponível | 409 | `BUSINESS_RULE_VIOLATION` (`purchase.insufficientLimit`) |
| Fatura do mês já fechada/paga | 409 | `BUSINESS_RULE_VIOLATION` (`purchase.cannotAddToInvoice`) |
| Sem token | 401 | `UNAUTHORIZED` |

---

## Regras de negócio (API)

Todas aplicadas em `PurchaseService.create` dentro de **uma `@Transactional`**
(`PurchaseService.java:61-120`) — falha em qualquer etapa desfaz tudo.

1. **Cartão do usuário e ACTIVE:** lookup `findByUser_IdAndId` → 404
   `creditCard.notFound` se inexistente/de outro usuário; `INACTIVE` → 409
   `creditCard.notActive`.
2. **Categoria ACTIVE e `EXPENSE`:** 404 `category.notFound` (lookup) → 409
   `category.notActive` → 409 `category.mustBeExpenseForPurchase`.
3. **Limite:** `totalAmount > creditLimit - usedLimit` (parcelas `OPEN`) → 409
   `purchase.insufficientLimit` ("Limite de crédito insuficiente").
4. **Parcelamento:** base = `total / n` com `RoundingMode.DOWN` (2 casas); o
   resto vai **tudo na última parcela**. Ex.: 100/3 → **33.33 / 33.33 / 33.34**
   (`PurchaseService.java:83-84,105-107`).
5. **Primeira fatura:** `purchaseDate.day <= closingDay` → mês da compra;
   senão → mês seguinte (`computeFirstInvoiceMonth`, `:137-142`).
6. **Fatura automática:** criada `OPEN` com `referenceMonth` = 1º do mês,
   `closingDate`/`dueDate` = dia do cartão com **clamp** para dias inexistentes
   (ex.: dia 31 em fevereiro → 28/29 — `withDayClamped`, `:174-178`). Parcela
   seguinte reutiliza a fatura do mês correspondente.
7. **Fatura não-OPEN:** se a fatura do mês já existe `CLOSED`/`PAID` → 409
   `purchase.cannotAddToInvoice` ("Não é possível adicionar compras a uma
   fatura já {fechada|paga} de {mês}", `:159-162`).
8. **Parcelas nascem `OPEN`** e incrementam `invoice.totalAmount` — limite já
   comprometido no momento da criação (`:108,115`).
9. **GET por id:** purchase inexistente ou de outro usuário → 404
   `purchase.notFound`.
10. **Listagem:** `Page` sort `purchaseDate DESC`; **sem filtros** além de
    paginação; **sem update/delete**.
11. **Validação:** JSR-303 → 400 `VALIDATION_ERROR` (+`fields`);
    `purchaseDate` malformada/JSON inválido → 400 `BAD_REQUEST`.

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** cartão ACTIVE com limite disponível e categoria EXPENSE ACTIVE
- **Quando** envio `POST /purchases` com `installmentsCount=1`
- **Então** recebo 201 com 1 parcela `OPEN`; `usedLimit` do cartão sobe em `totalAmount`

- **Dado** os mesmos recursos, `totalAmount=100`, `installmentsCount=3`
- **Quando** envio `POST /purchases`
- **Então** recebo 201 com parcelas **33.33 / 33.33 / 33.34** (soma = 100) em
  `referenceMonth` consecutivos

- **Dado** compras em datas distintas
- **Quando** envio `GET /purchases`
- **Então** recebo 200 `Page` ordenado por `purchaseDate DESC`

- **Dado** uma purchase existente
- **Quando** envio `GET /purchases/{id}`
- **Então** recebo 200 com `installments[]` completos

### Casos negativos / borda
- **Dado** `purchaseDate.day <= closingDay` do cartão
- **Quando** envio a compra
- **Então** a 1ª fatura (`installments[0].referenceMonth`) é o **mês da compra**

- **Dado** `purchaseDate.day > closingDay`
- **Quando** envio a compra
- **Então** a 1ª fatura é o **mês seguinte** ao da compra

- **Dado** `totalAmount` maior que `creditLimit - usedLimit`
- **Quando** envio `POST /purchases`
- **Então** recebo 409 (`purchase.insufficientLimit`) e nada é criado

- **Dado** cartão INACTIVE
- **Quando** envio `POST /purchases`
- **EntÃO** recebo 409 (`creditCard.notActive`)

- **Dado** cartão inexistente ou de outro usuário
- **Quando** envio `POST /purchases`
- **Então** recebo 404 (`creditCard.notFound`)

- **Dado** categoria INACTIVE
- **Quando** envio `POST /purchases`
- **Então** recebo 409 (`category.notActive`)

- **Dado** categoria `INCOME` ACTIVE
- **Quando** envio `POST /purchases`
- **Então** recebo 409 (`category.mustBeExpenseForPurchase`)

- **Dado** categoria inexistente ou de outro usuário
- **Quando** envio `POST /purchases`
- **Então** recebo 404 (`category.notFound`)

- **Dado** a fatura do mês-alvo com status `CLOSED` ou `PAID`
  (pré-condição: compra anterior + `POST /invoices/{id}/close` ou `/pay`)
- **Quando** envio nova compra cuja 1ª parcela cai nesse mês
- **Então** recebo 409 (`purchase.cannotAddToInvoice`) e nada é criado

- **Dado** compra criada (parcelas `OPEN`)
- **Quando** consulto `GET /credit-cards/{id}`
- **Então** `usedLimit` considera as novas parcelas (`availableLimit` cai)

- **Dado** `{id}` inexistente ou purchase de outro usuário
- **Quando** envio `GET /purchases/{id}`
- **Então** recebo 404 (`purchase.notFound`)

- **Dado** campos obrigatórios ausentes, `totalAmount` ≤ 0,
  `installmentsCount` < 1 ou `description` > 255
- **Quando** envio `POST /purchases`
- **Então** recebo 400 `VALIDATION_ERROR` com `fields`

- **Dado** `purchaseDate` malformada ou JSON inválido
- **Quando** envio a requisição
- **Então** recebo 400 `BAD_REQUEST`

- **Dado** token ausente
- **Quando** chamo qualquer endpoint de `/purchases`
- **Então** recebo 401 `UNAUTHORIZED`

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/CreditCardsPage.tsx:103-123,392-541`,
`frontend/src/services/purchaseService.ts:13-19`.

**Não existe rota própria de compras.** O fluxo de compra vive em `/cartoes`
(`CreditCardsPage`), no form `credit-cards-purchase-form`. A **fatura gerada
não tem tela**: `pages/InvoicesPage.tsx` existe com testids completos mas **não
está roteada** (`AppRoutes.tsx` não a importa) — casos que exigem *ver* a fatura
pela UI viram `[-]`.

### Campos (form de compra em `/cartoes`)

| Campo | testid | Validação client |
|---|---|---|
| Cartão | `credit-cards-purchase-card-select` | `required`; só cartões ACTIVE (label com disponível) |
| Categoria | `credit-cards-purchase-category-select` | `required`; só ACTIVE `EXPENSE` |
| Descrição | `credit-cards-purchase-description-input` | `maxLength=255`, opcional |
| Valor total | `credit-cards-purchase-amount-input` | `required`, `min=0.01` `step=0.01` |
| Data | `credit-cards-purchase-date-input` | `required`, **sem `max`** (data futura aceita) |
| Parcelas | `credit-cards-purchase-installments-input` | `required`, `min=1` `max=48` |
| Preview parcela | hint no FormField | `total/count` client-side |
| Submit | `credit-cards-purchase-submit-btn` | — |

### Comportamento
- **Sucesso:** `message-success` com nº de parcelas
  (`cards.purchaseRegistered {n}`), form reseta (mantém cartão selecionado),
  recarrega `credit-cards-purchases-list` **e** cartões (resumo muda).
- **Erro da API** (limite/fatura/cartão inativo): `message-error`; permanece na
  página; nenhuma lista muda.
- **Lista de compras recentes:** `GET /purchases?page=0&size=10`; item
  `credit-cards-purchase-{id}` mostra descrição, cartão, categoria, `Nx` e
  valor; vazio → `credit-cards-purchases-empty`.

### data-testids relevantes
`credit-cards-purchase-form`, `credit-cards-purchase-card-select`,
`credit-cards-purchase-category-select`,
`credit-cards-purchase-{description,amount,date,installments}-input`,
`credit-cards-purchase-submit-btn`, `credit-cards-purchases-list`,
`credit-cards-purchase-{id}`, `credit-cards-purchases-empty`,
`credit-cards-summary-{limit,used,available}`, `message-{error,success}`.

---

## Regras de negócio (UI)

1. **Compra só via `/cartoes`:** não há rota/`Page` dedicada de compras.
2. **Selects restritos:** cartões ACTIVE (com disponível no label) e categorias
   ACTIVE `EXPENSE` — pré-validação client das regras 1–2 da API.
3. **Sucesso:** `message-success` com `n` de parcelas; recarrega lista de
   compras **e** cartões (resumo `used/available` atualiza).
4. **Erro da API** (limite insuficiente, cartão inativo, fatura fechada) →
   `message-error`; permanece na página; nada é criado.
5. **Validação nativa:** `required`/`min`/`max` bloqueiam submit inválido sem
   tocar a API.
6. **Fatura sem tela:** não é possível ver/fechar/pagar a fatura gerada pela UI
   (`InvoicesPage` órfã) — só via dashboard/planejamento ou API.
7. **Sem editar/excluir compra** na UI (espelha a API).
8. **Data futura aceita** no form de compra (sem `max`; backend não valida).

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** cartão ACTIVE e categoria EXPENSE ACTIVE; tela `/cartoes`
- **Quando** preencho cartão, categoria, valor, data, parcelas e submeto
- **Então** `message-success` com o nº de parcelas, item
  `credit-cards-purchase-{id}` na lista e resumo `used/available` atualizado

- **Dado** compra de 100 em 3x registrada
- **Quando** observo o hint do form
- **Então** o preview de valor por parcela reflete `total/parcelas` (client-side)

### Casos negativos / borda
- **Dado** valor acima do disponível no cartão
- **Quando** submeto
- **Então** `message-error` com a mensagem 409 da API; sem item novo; resumo inalterado

- **Dado** campos obrigatórios vazios
- **Quando** tento submeter
- **Então** validação nativa bloqueia; nenhuma chamada a `/purchases`

- **Dado** cartão INACTIVE e categoria INCOME cadastradas via API
- **Quando** abro os selects
- **EntÃO** nenhuma delas aparece como opção

- **Dado** usuário sem compras
- **Quando** a seção de compras carrega
- **Então** `credit-cards-purchases-empty` visível

- **Dado** data futura escolhida no form
- **Quando** submeto payload válido
- **Então** a compra é criada (UI/backend não bloqueiam data futura)

- **Dado** compra recém-criada
- **Quando** observo `credit-cards-summary-used`/`-available`
- **Então** os valores refletem o novo `usedLimit`

---

## Limitações relevantes (viram casos `[-]`)

- **Sem update/delete de compra** (API e UI).
- **Fatura criada automaticamente, sem tela:** `InvoicesPage` órfã — ver/fechar/
  pagar fatura pela UI é impossível; `POST /invoices` (create) não existe.
- **Sem filtros** de compra além de paginação (API) — a UI fixa `page=0&size=10`.

---

## Referência (código-fonte — app finance-control)

- `backend/src/main/java/br/com/financecontrol/controller/PurchaseController.java:37-56`
- `backend/src/main/java/br/com/financecontrol/service/PurchaseService.java:61-178`
- `backend/src/main/java/br/com/financecontrol/dto/purchase/PurchaseRequest.java:13-37`
- `backend/src/main/java/br/com/financecontrol/dto/purchase/PurchaseResponse.java:9-21`
- `backend/src/main/java/br/com/financecontrol/dto/purchase/InstallmentResponse.java:9-16`
- `backend/src/main/java/br/com/financecontrol/mapper/PurchaseMapper.java:15-43`
- `backend/src/main/java/br/com/financecontrol/controller/InvoiceController.java:37-64`
- `backend/src/main/java/br/com/financecontrol/service/InvoiceService.java:55-95`
- `backend/src/main/java/br/com/financecontrol/exception/GlobalExceptionHandler.java:27-56`
- `backend/src/main/resources/messages.properties:28,32-34,63-74,80`
- `frontend/src/pages/CreditCardsPage.tsx:103-123,392-541`
- `frontend/src/services/purchaseService.ts:13-19`
- `frontend/src/routes/AppRoutes.tsx:14-38` (InvoicesPage ausente)
- `frontend/src/pages/InvoicesPage.tsx` (órfã, inacessível)
