# História: Transferências

## Descrição

Permite mover dinheiro entre duas contas do usuário. Cria uma transferência
(`POST /transfers`), lista paginada (`GET /transfers`) e busca por id
(`GET /transfers/{id}`). Não há filtros além da paginação e **não existem**
update/delete: a transferência é imutável após a criação.

O efeito no saldo é atômico: débito na conta de origem + crédito na conta de
destino na mesma transação de banco. **Não há validação de saldo insuficiente** —
a origem pode ficar com saldo negativo.

O fluxo cobre validação JSR-303 do payload, regras de negócio (contas distintas,
contas ativas), ownership (contas de outro usuário → 404) e a integridade dos
saldos pós-transferência.

---

## API

### Endpoints

- **`POST /transfers`** → **201 Created** (autenticação requerida: **sim**)
- **`GET /transfers`** → **200 OK** (`Page<TransferResponse>`, sort `transferDate DESC`)
- **`GET /transfers/{id}`** → **200 OK** / **404 Not Found** (autenticação requerida: **sim**)
- `PUT/DELETE /transfers/{id}` — **não existem** (sem `@PutMapping`/`@DeleteMapping`)

### Request — `POST /transfers`

```json
{
  "sourceAccountId": "<uuid>",           // Obrigatório (@NotNull)
  "destinationAccountId": "<uuid>",      // Obrigatório (@NotNull)
  "amount": 200.00,                      // Obrigatório, DecimalMin 0.01
  "transferDate": "2026-09-12",          // Obrigatório (LocalDate yyyy-MM-dd)
  "description": "Aluguel"               // Opcional, ≤ 255
}
```

Validação JSR-303 (`TransferRequest`): `sourceAccountId.required`,
`destinationAccountId.required`, `amount.required`,
`amount.greaterThanZero`, `transferDate.required`, `description.tooLong`.

### Response — `TransferResponse` (201 e 200)

```json
{
  "id": "<uuid>",
  "sourceAccountId": "<uuid>",
  "sourceAccountName": "Conta Corrente",
  "destinationAccountId": "<uuid>",
  "destinationAccountName": "Poupança",
  "amount": 200.00,
  "transferDate": "2026-09-12",
  "description": "Aluguel",
  "createdAt": "2026-09-12T10:30:00"
}
```

`GET /transfers` envolve o mesmo shape dentro de um `Page` do Spring Data:
`{content: [TransferResponse...], totalElements, totalPages, number, size, ...}`.

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

`fields` (map campo→mensagem) só é preenchido em 400 de validação. Erros chegam
traduzidos para pt-BR.

### Status usados nesta feature

| Cenário | HTTP | `error` |
|---|---|---|
| Transferência criada | 201 | — |
| Listagem / busca por id | 200 | — |
| Campos ausentes / `amount` < 0.01 / `description` > 255 | 400 | `VALIDATION_ERROR` (+ `fields`) |
| `transferDate` malformada / JSON inválido | 400 | `BAD_REQUEST` |
| Conta de outro usuário (origem/destino) ou id inexistente | 404 | `NOT_FOUND` |
| Origem = destino | 409 | `BUSINESS_RULE_VIOLATION` (`transfer.sameAccount`) |
| Conta de origem inativa | 409 | `BUSINESS_RULE_VIOLATION` (`transfer.sourceNotActive`) |
| Conta de destino inativa | 409 | `BUSINESS_RULE_VIOLATION` (`transfer.destinationNotActive`) |
| Sem token / token inválido | 401 | `UNAUTHORIZED` |

---

## Regras de negócio (API)

1. **Contas distintas:** `sourceAccountId == destinationAccountId` → 409
   (`transfer.sameAccount` — "As contas de origem e destino devem ser diferentes").
   Checagem ocorre **depois** das duas buscas por ownership (`TransferService.java:78-81`).
2. **Origem ativa:** conta de origem `INACTIVE` → 409 (`transfer.sourceNotActive`).
3. **Destino ativo:** conta de destino `INACTIVE` → 409 (`transfer.destinationNotActive`).
4. **Ownership:** conta de outro usuário → 404 `account.sourceNotFound` /
   `account.destinationNotFound` (busca `findByUser_IdAndId`; nunca 403).
5. **Atomicidade:** débito origem + crédito destino + gravação da transferência
   numa única `@Transactional` (`TransferService.java:50-63`) — ou tudo, ou nada.
6. **Sem validação de saldo:** saldo da origem **pode ficar negativo** (não há
   regra de saldo insuficiente; DB sem CHECK de balance).
7. **Listagem:** `Page` com sort default `transferDate DESC`
   (`@PageableDefault`, `TransferController.java:47`); **sem filtros** além de
   `page`/`size`.
8. **Imutabilidade:** sem `PUT`/`PATCH`/`DELETE` — transferência não editável
   nem removível.
9. **GET por id:** id inexistente **ou** transferência de outro usuário → 404
   (`transfer.notFound`).
10. **Validação:** JSR-303 violada → 400 `VALIDATION_ERROR` (+`fields`);
    `transferDate` fora do formato `yyyy-MM-dd` / JSON malformado → 400
    `BAD_REQUEST`.

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** duas contas ACTIVE do usuário com saldo inicial conhecido
- **Quando** envio `POST /transfers` com origem ≠ destino, `amount` ≥ 0.01 e data válida
- **Então** recebo 201 com `TransferResponse` completo; saldo da origem diminui e o do destino aumenta em `amount`

- **Dado** transferências criadas em datas diferentes
- **Quando** envio `GET /transfers`
- **Então** recebo 200 com `Page` ordenado por `transferDate DESC`

- **Dado** uma transferência existente do usuário
- **Quando** envio `GET /transfers/{id}`
- **Então** recebo 200 com o mesmo objeto do 201

### Casos negativos / borda
- **Dado** origem = destino (mesmo uuid)
- **Quando** envio `POST /transfers`
- **Então** recebo 409 (`transfer.sameAccount`) e **nenhum saldo muda**

- **Dado** conta de origem INACTIVE
- **Quando** envio `POST /transfers`
- **Então** recebo 409 (`transfer.sourceNotActive`)

- **Dado** conta de destino INACTIVE
- **Quando** envio `POST /transfers`
- **Então** recebo 409 (`transfer.destinationNotActive`)

- **Dado** `sourceAccountId`/`destinationAccountId` pertencente a outro usuário
- **Quando** envio `POST /transfers`
- **Então** recebo 404 `NOT_FOUND` (`account.sourceNotFound`/`account.destinationNotFound`)

- **Dado** id inexistente ou de outro usuário
- **Quando** envio `GET /transfers/{id}`
- **Então** recebo 404 (`transfer.notFound`)

- **Dado** campo obrigatório ausente, `amount` ≤ 0 ou `description` > 255
- **Quando** envio `POST /transfers`
- **Então** recebo 400 `VALIDATION_ERROR` com `fields`

- **Dado** `transferDate` malformada (ex.: `"12/09/2026"`)
- **Quando** envio `POST /transfers`
- **Então** recebo 400 `BAD_REQUEST`

- **Dado** saldo da origem menor que `amount`
- **Quando** envio `POST /transfers` válida
- **Então** recebo 201 e o saldo da origem fica **negativo** (regra 6)

- **Dado** token ausente
- **Quando** chamo qualquer endpoint de `/transfers`
- **Então** recebo 401 `UNAUTHORIZED`

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/TransfersPage.tsx`, `frontend/src/services/transferService.ts`,
`frontend/src/routes/AppRoutes.tsx:33`.

### Rotas e telas
- `/transferencias` → `TransfersPage` (testid raiz `transfers-page`), dentro de
  `Layout` (rota protegida).
- A tela tem **dois** pontos de criação que compartilham o **mesmo handler e
  estado**: form rápido inline (`transfers-quick-form`) e modal
  (`transfers-modal`) aberto por `transfers-page-new-btn`.

### Campos do formulário (form rápido e modal espelham-se)

| Campo | testid (quick) | testid (modal) | Validação client |
|---|---|---|---|
| Conta origem | `transfers-quick-form-source-select` | `transfers-modal-source-select` | `required` nativo; só contas ACTIVE |
| Conta destino | `transfers-quick-form-destination-select` | `transfers-modal-destination-select` | `required` nativo; só contas ACTIVE |
| Valor | `transfers-quick-form-amount-input` | `transfers-modal-amount-input` | `required`, `type=number` `min=0.01` `step=0.01` |
| Data | `transfers-quick-form-date-input` | `transfers-modal-date-input` | `required`, `max={today()}` |
| Descrição | `transfers-quick-form-description-input` | `transfers-modal-description-input` | `maxLength=255`, opcional |
| Submit | `transfers-quick-form-submit-btn` | `transfers-modal-submit-btn` | — |
| Cancelar (modal) | — | `transfers-modal-cancel-btn` | fecha sem salvar |

### Comportamento
- **Sucesso:** fecha modal (se aberto), zera o form, recarrega a lista de
  transferências **e** as contas (`TransfersPage.tsx:63-66`).
- **Erro da API:** exibido em `Message` (`message-error`); no modal também em
  `transfers-modal-error`. Permanece na página (não navega).
- **Selects:** apenas contas `ACTIVE` (`filter(a => a.status === 'ACTIVE')`,
  linha 49).
- **Lista:** `transfers-list-item-{id}` com "origem → destino", data, descrição
  e valor formatado; empty state `transfers-list-empty`; loading
  `transfers-list-loading`.
- **Paginação:** `transfers-pagination-{prev,next}` só renderiza se
  `totalPages > 1`; `prev` desabilitado em `page <= 0` (size fixo 20).

### data-testids relevantes
`transfers-page`, `transfers-page-header`, `transfers-page-new-btn`,
`transfers-quick-form-card`, `transfers-quick-form`,
`transfers-quick-form-{source,destination}-select`,
`transfers-quick-form-{amount,date,description}-input`,
`transfers-quick-form-submit-btn`, `transfers-modal`,
`transfers-modal-content`, `transfers-modal-title`, `transfers-modal-form`,
`transfers-modal-{source,destination}-select`,
`transfers-modal-{amount,date,description}-input`,
`transfers-modal-{cancel,submit}-btn`, `transfers-modal-error`,
`transfers-list-card`, `transfers-list`, `transfers-list-item-{id}`,
`transfers-list-empty`, `transfers-list-loading`,
`transfers-pagination-{prev,next}`.

---

## Regras de negócio (UI)

1. **Selects só contas ACTIVE:** origem e destino listam exclusivamente contas
   ativas do usuário.
2. **Form rápido e modal compartilham estado/handler único** (`form` +
   `handleSubmit`); sucesso recarrega lista **e** contas.
3. **Data máxima = hoje:** inputs de data usam `max={today()}` — data futura
   é bloqueada pelo browser.
4. **Erro da API** exibido em `Message` (`message-error`) e, no modal, também em
   `transfers-modal-error`; permanece na página.
5. **Paginação condicional:** botões só aparecem com `totalPages > 1`; `prev`
   desabilitado na primeira página.
6. **Empty state** (`transfers-list-empty`) quando não há transferências.
7. **Validação nativa:** campos com `required`/`min` bloqueiam o submit antes
   de tocar a API.
8. **Sem edição/exclusão na UI:** não há botões de editar/excluir — só criar e
   listar (espelha a API).
9. **Sem filtros/busca na UI:** apenas paginação `page`/`size` (size fixo 20).

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** duas contas ACTIVE cadastradas via API e a tela `/transferencias`
- **Quando** preencho origem, destino, valor e data no form rápido e submeto
- **Então** o novo item aparece em `transfers-list-item-{id}` com os nomes das
  contas, valor e data corretos

- **Dado** a mesma tela com o modal aberto (`transfers-page-new-btn`)
- **Quando** preencho o `transfers-modal-form` e submeto
- **Então** o modal fecha, a lista recarrega e o novo item está visível

### Casos negativos / borda
- **Dado** origem = destino selecionadas no form
- **Quando** submeto
- **Então** `message-error` exibe a mensagem 409 da API e nenhum item novo é criado

- **Dado** campos obrigatórios vazios
- **Quando** tento submeter
- **Então** a validação nativa impede o submit e **nenhuma chamada** é feita a `/transfers`

- **Dado** usuário sem transferências
- **Quando** a lista carrega vazia
- **Então** `transfers-list-empty` está visível

- **Dado** uma conta INACTIVE cadastrada via API
- **Quando** abro os selects de origem/destino
- **Então** a conta inativa **não** aparece nas opções

- **Dado** campo de data com data futura
- **Quando** tento submeter
- **Então** o browser bloqueia (`max={today()}) e a API não é chamada

- **Dado** mais de uma página de transferências
- **Quando** clico em `transfers-pagination-next`
- **Então** a página avança; na primeira página `prev` está desabilitado

- **Dado** modal aberto com dados preenchidos
- **Quando** clico em `transfers-modal-cancel-btn`
- **Então** o modal fecha sem criar transferência

---

## Referência (código-fonte — app finance-control)

- `backend/src/main/java/br/com/financecontrol/controller/TransferController.java:37-56`
- `backend/src/main/java/br/com/financecontrol/service/TransferService.java:38-88`
- `backend/src/main/java/br/com/financecontrol/dto/transfer/TransferRequest.java:12-30`
- `backend/src/main/java/br/com/financecontrol/dto/transfer/TransferResponse.java:8-18`
- `backend/src/main/java/br/com/financecontrol/mapper/TransferMapper.java:11-23`
- `backend/src/main/java/br/com/financecontrol/exception/GlobalExceptionHandler.java:27-56`
- `backend/src/main/resources/messages.properties:16-21,66-69,76,80`
- `frontend/src/pages/TransfersPage.tsx:25-413`
- `frontend/src/services/transferService.ts:12-18`
- `frontend/src/routes/AppRoutes.tsx:33`
- `frontend/src/components/Message.tsx:17-27`
