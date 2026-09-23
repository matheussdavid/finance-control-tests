# História: Cartões de Crédito

## Descrição

CRUD (sem delete) de cartões de crédito: criar (`POST`), listar (`GET`),
buscar por id (`GET/{id}`), atualizar (`PUT/{id}`) e desativar
(`PATCH/{id}/deactivate` → 204). As respostas trazem **`usedLimit`** (soma das
parcelas `OPEN`) e **`availableLimit`** (`creditLimit - usedLimit`) calculados
dinamicamente — não confundir com o limite cadastrado.

Cartão nasce `ACTIVE`; a desativação é **terminal** (não existe endpoint de
reativação; re-desativar → 409). Cartão inativo não recebe compras novas
(409 `creditCard.notActive` — regra verificada no fluxo de `POST /purchases`).
Alterar `closingDay`/`dueDay` **não** altera faturas já existentes (as faturas
guardam `closingDate`/`dueDate` próprios).

---

## API

### Endpoints

- **`POST /credit-cards`** → **201 Created** (autenticação requerida: **sim**)
- **`GET /credit-cards`** → **200 OK** (`List<CreditCardResponse>`, ordenado por nome)
- **`GET /credit-cards/{id}`** → **200 OK** / **404**
- **`PUT /credit-cards/{id}`** → **200 OK** / **404**
- **`PATCH /credit-cards/{id}/deactivate`** → **204 No Content** / **404** / **409**

### Request — `POST /credit-cards` e `PUT /credit-cards/{id}`

```json
{
  "name": "Visa",                 // Obrigatório (@NotBlank), ≤ 100
  "creditLimit": 3000.00,         // Obrigatório, DecimalMin 0.01
  "closingDay": 10,               // Obrigatório, Min 1, Max 31
  "dueDay": 15                    // Obrigatório, Min 1, Max 31
}
```

Validação JSR-303 (create e update idênticos): `name.required`, `name.tooLong`,
`creditLimit.required`, `creditLimit.greaterThanZero`, `closingDay.required`,
`dueDay.required`, `day.between1and31`.

### Response — `CreditCardResponse` (201/200)

```json
{
  "id": "<uuid>",
  "name": "Visa",
  "creditLimit": 3000.00,
  "usedLimit": 100.00,
  "availableLimit": 2900.00,
  "closingDay": 10,
  "dueDay": 15,
  "status": "ACTIVE",
  "createdAt": "2026-09-12T10:30:00",
  "updatedAt": "2026-09-12T10:30:00"
}
```

- `POST` retorna `usedLimit = 0` e `availableLimit = creditLimit`
  (`CreditCardService.java:49`).
- `GET` (lista/id) e `PUT` calculam `usedLimit` via
  `InstallmentRepository.sumByCardAndStatus(cardId, OPEN)` e
  `availableLimit = creditLimit - usedLimit` (`CreditCardMapper.java:14`).
- `PATCH .../deactivate` → **204 sem corpo**.

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
| Criação / leitura / atualização | 201/200 | — |
| Desativação | 204 | — (corpo vazio) |
| Campos ausentes / limites de validação | 400 | `VALIDATION_ERROR` (+ `fields`) |
| JSON malformado / `{id}` não-UUID | 400 | `BAD_REQUEST` |
| `{id}` inexistente ou de outro usuário | 404 | `NOT_FOUND` (`creditCard.notFound`) |
| Re-desativar cartão já inativo | 409 | `BUSINESS_RULE_VIOLATION` (`creditCard.alreadyInactive`) |
| Compra em cartão inativo (`POST /purchases`) | 409 | `BUSINESS_RULE_VIOLATION` (`creditCard.notActive`) |
| Sem token | 401 | `UNAUTHORIZED` |

---

## Regras de negócio (API)

1. **`usedLimit` dinâmico:** `SUM(amount)` das parcelas com status `OPEN` do
   cartão (`CreditCardService.java:91-93`) — muda conforme compras/pagamentos.
2. **`availableLimit` = `creditLimit - usedLimit`** (`CreditCardMapper.java:14`).
   **Não** é o limite cadastrado: `creditLimit` é o cadastrado; `availableLimit`
   é o calculado.
3. **Cartão inativo não recebe compra:** `POST /purchases` com cartão
   `INACTIVE` → 409 `creditCard.notActive` (pré-condição verificada em outro
   recurso — `PurchaseService.java:65-67`).
4. **Re-desativar → 409** `creditCard.alreadyInactive` (`CreditCardService.java:79-81`).
5. **Desativação terminal:** não existe `POST/PUT` de reativação; `INACTIVE` é
   estado final (sem endpoint de reverter).
6. **`PUT` não altera datas de faturas existentes:** faturas guardam
   `closingDate`/`dueDate` próprios (`entity/Invoice.java`) — mudar
   `closingDay`/`dueDay` só afeta faturas futuras criadas depois.
7. **Ownership:** `{id}` de outro usuário → 404 `creditCard.notFound`
   (`findByUser_IdAndId`, `CreditCardService.java:86-89`) — nunca 403.
8. **Validação:** JSR-303 → 400 `VALIDATION_ERROR` (+`fields`);
   JSON malformado/`{id}` não-UUID → 400 `BAD_REQUEST`.
9. **Listagem sem paginação:** `GET /credit-cards` retorna `List` completa
   (ordenada por nome), não `Page`.

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** payload válido (name, creditLimit ≥ 0.01, days 1–31)
- **Quando** envio `POST /credit-cards`
- **Então** recebo 201 com `status=ACTIVE`, `usedLimit=0` e `availableLimit=creditLimit`

- **Dado** cartões criados com nomes distintos
- **Quando** envio `GET /credit-cards`
- **Então** recebo 200 com array ordenado por nome e limites calculados

- **Dado** um cartão existente
- **Quando** envio `GET /credit-cards/{id}`
- **Então** recebo 200 com o mesmo shape do 201 (mas `usedLimit` recalculado)

- **Dado** um cartão existente
- **Quando** envio `PUT /credit-cards/{id}` com novos valores
- **Então** recebo 200 com os campos atualizados

- **Dado** um cartão ACTIVE
- **Quando** envio `PATCH /credit-cards/{id}/deactivate`
- **Então** recebo 204 sem corpo; `GET /credit-cards/{id}` retorna `status=INACTIVE`

### Casos negativos / borda
- **Dado** uma compra OPEN registrada no cartão
- **Quando** consulto `GET /credit-cards/{id}`
- **Então** `usedLimit` = soma das parcelas OPEN e `availableLimit = creditLimit - usedLimit`

- **Dado** cartão já INACTIVE
- **Quando** envio `PATCH .../deactivate` novamente
- **Então** recebo 409 (`creditCard.alreadyInactive`)

- **Dado** cartão INACTIVE (via API)
- **Quando** envio `POST /purchases` apontando para ele
- **Então** recebo 409 (`creditCard.notActive`) e nada é criado

- **Dado** `{id}` inexistente ou de outro usuário
- **Quando** envio `GET`/`PUT`/`PATCH` com esse id
- **Então** recebo 404 (`creditCard.notFound`)

- **Dado** campo obrigatório ausente, `name` > 100, `creditLimit` ≤ 0 ou
  `closingDay`/`dueDay` fora de 1–31
- **Quando** envio `POST` ou `PUT`
- **Então** recebo 400 `VALIDATION_ERROR` com `fields`

- **Dado** JSON malformado ou `{id}` não-UUID na URL
- **Quando** envio a requisição
- **Então** recebo 400 `BAD_REQUEST`

- **Dado** fatura já existente com `closingDate`/`dueDate` derivados do cartão
- **Quando** envio `PUT` alterando `closingDay`/`dueDay`
- **Então** recebo 200 e a fatura existente **mantém** as datas originais

- **Dado** token ausente
- **Quando** chamo qualquer endpoint de `/credit-cards`
- **Então** recebo 401 `UNAUTHORIZED`

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/CreditCardsPage.tsx`,
`frontend/src/services/creditCardService.ts`, `frontend/src/routes/AppRoutes.tsx:29`.

### Rotas e telas
- `/cartoes` → `CreditCardsPage` (testid raiz `credit-cards-page`), rota
  protegida. A mesma tela concentra: resumo, lista de cartões, form de cartão
  (criar/editar), form de **compra** e lista de compras recentes.

### Campos — form de cartão

| Campo | testid | Validação client |
|---|---|---|
| Name | `credit-cards-form-name-input` | `required`, `maxLength=100` |
| Limite | `credit-cards-form-limit-input` | `required`, `type=number` `min=0.01` `step=0.01` |
| Dia fechamento | `credit-cards-form-closing-input` | `required`, `min=1` `max=31` |
| Dia vencimento | `credit-cards-form-due-input` | `required`, `min=1` `max=31` |
| Submit | `credit-cards-form-submit-btn` | cria (POST) ou salva (PUT) conforme `editingId` |
| Cancelar edição | `credit-cards-form-cancel-btn` | só aparece em modo edição |

### Campos — form de compra (pertence a esta rota; casos em `compras`)

| Campo | testid |
|---|---|
| Cartão | `credit-cards-purchase-card-select` (só ACTIVE, label com disponível) |
| Categoria | `credit-cards-purchase-category-select` (só ACTIVE `EXPENSE`) |
| Descrição | `credit-cards-purchase-description-input` (`maxLength=255`) |
| Valor total | `credit-cards-purchase-amount-input` (`min=0.01`) |
| Data | `credit-cards-purchase-date-input` (**sem `max`** — data futura aceita) |
| Parcelas | `credit-cards-purchase-installments-input` (`min=1` `max=48`, com hint de valor/parcela) |
| Submit | `credit-cards-purchase-submit-btn` |

### Comportamento
- **Resumo:** `credit-cards-summary-{limit,used,available}` soma
  `creditLimit`/`usedLimit`/`availableLimit` **apenas dos cartões ACTIVE**
  (`CreditCardsPage.tsx:73-75`).
- **Criar:** sucesso → `message-success` (`cards.cardCreated`), zera form,
  recarrega lista. **Editar:** botão `...-edit-btn` preenche o form
  (`editingId`); submit faz `PUT`; `credit-cards-form-cancel-btn` zera.
- **Desativar:** `...-deactivate-btn` só existe em cartões ACTIVE; sucesso →
  `message-success`, recarrega; item fica `opacity-60` e perde os botões.
- **Compra:** sucesso → `message-success` com nº de parcelas
  (`cards.purchaseRegistered`), recarrega compras **e** cartões (resumo muda).
- **Erro da API:** `message-error` (formatado por `formatApiError`); permanece
  na página.
- **Listas:** `credit-cards-list-item-{id}`; compras recentes =
  `GET /purchases?page=0&size=10`; empty states `credit-cards-list-empty` /
  `credit-cards-purchases-empty`.

### data-testids relevantes
`credit-cards-page`, `credit-cards-page-header`, `credit-cards-page-new-btn`,
`credit-cards-summary`, `credit-cards-summary-{limit,used,available}`,
`credit-cards-list`, `credit-cards-list-item-{id}`,
`credit-cards-list-item-{id}-{edit,deactivate}-btn`, `credit-cards-list-empty`,
`credit-cards-form`, `credit-cards-form-{name,limit,closing,due}-input`,
`credit-cards-form-{submit,cancel}-btn`, `credit-cards-purchase-form`,
`credit-cards-purchase-{card,category}-select`,
`credit-cards-purchase-{description,amount,date,installments}-input`,
`credit-cards-purchase-submit-btn`, `credit-cards-purchases-list`,
`credit-cards-purchase-{id}`, `credit-cards-purchases-empty`,
`message-{error,success}`.

---

## Regras de negócio (UI)

1. **Resumo só ACTIVE:** `summary-{limit,used,available}` somam apenas cartões
   ativos.
2. **Form cria ou edita:** `editingId` decide POST vs PUT; cancelar zera o modo
   edição e o formulário.
3. **Ações só em cartões ACTIVE:** botões `edit`/`deactivate` não renderizam
   para cartão inativo (item fica esmaecido).
4. **Select de compra só cartões ACTIVE** (label exibe nome + disponível);
   **select de categoria só ACTIVE `EXPENSE`**.
5. **Sucesso** exibe `message-success` (chaves i18n `cards.*`) e recarrega
   listas/resumo; **erro** exibe `message-error` e permanece.
6. **Após compra:** recarrega compras e cartões → resumo `used/available`
   atualiza sem reload da página.
7. **Preview de parcela:** hint `cards.perInstallment` quando valor e nº de
   parcelas preenchidos (cálculo client `amount/count`).
8. **Data da compra sem `max`:** diferente de transferências/QuickCapture, o
   form de compra aceita data futura (backend também não valida).
9. **Compras recentes:** `GET /purchases?page=0&size=10` (mais recentes primeiro).

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** usuário autenticado em `/cartoes`
- **Quando** preencho name, limite, dia fechamento/vencimento e submeto
- **Então** `message-success` aparece e `credit-cards-list-item-{id}` é criado
  com `usedLimit=0`

- **Dado** cartão criado e categorias EXPENSE ativas
- **Quando** registro uma compra válida no form de compra
- **Então** `message-success` com o nº de parcelas, item em
  `credit-cards-purchase-{id}` e resumo `used/available` atualizado

- **Dado** cartão existente
- **Quando** clico em `...-edit-btn`, altero o nome e submeto
- **Então** `message-success` e o item da lista mostra o novo nome/limite

- **Dado** cartão ACTIVE
- **Quando** clico em `...-deactivate-btn`
- **Então** `message-success`, status `INATIVE` no badge e botões de ação somem

### Casos negativos / borda
- **Dado** campos obrigatórios do form de cartão vazios
- **Quando** tento submeter
- **Então** validação nativa bloqueia; nenhuma chamada à API

- **Dado** compra com valor acima do disponível
- **Quando** submeto `credit-cards-purchase-form`
- **Então** `message-error` com a mensagem 409 da API; nenhuma compra criada;
  resumo inalterado

- **Dado** cartão INACTIVE e categorias inativas cadastradas via API
- **Quando** abro os selects de compra
- **Então** cartão inativo e categorias inativas/INCOME **não** aparecem

- **Dado** usuário só com cartões INACTIVE
- **Quando** a página carrega
- **Então** resumo mostra zeros (só ACTIVE somam) e lista mostra o item esmaecido

- **Dado** form de compra com valor e parcelas preenchidos
- **Quando** altero um dos campos
- **Então** o hint de valor por parcela é atualizado

- **Dado** usuário sem compras
- **Quando** a seção de compras carrega
- **Então** `credit-cards-purchases-empty` está visível

- **Dado** cartão em modo edição
- **Quando** clico em `credit-cards-form-cancel-btn`
- **Então** form volta ao modo criação, limpo

---

## Limitações relevantes (viram casos `[-]`)

- **Desativação terminal:** não existe reativação (UI esconde botão; API → 409).
- **Sem exclusão:** nenhum `DELETE` de cartão/compra na UI nem na API.
- **Fatura gerada por compra não tem tela:** `InvoicesPage` existe mas **não está
  roteada** (`AppRoutes.tsx` não a importa) — visualização de fatura pela UI é
  impossível (relacionado a `compras`).

---

## Referência (código-fonte — app finance-control)

- `backend/src/main/java/br/com/financecontrol/controller/CreditCardController.java:37-71`
- `backend/src/main/java/br/com/financecontrol/service/CreditCardService.java:38-93`
- `backend/src/main/java/br/com/financecontrol/dto/creditcard/CreditCardRequest.java:13-35`
- `backend/src/main/java/br/com/financecontrol/dto/creditcard/CreditCardUpdateRequest.java:12-30`
- `backend/src/main/java/br/com/financecontrol/dto/creditcard/CreditCardResponse.java:9-20`
- `backend/src/main/java/br/com/financecontrol/mapper/CreditCardMapper.java:13-27`
- `backend/src/main/java/br/com/financecontrol/service/PurchaseService.java:63-81`
- `backend/src/main/java/br/com/financecontrol/entity/Invoice.java:28-32`
- `backend/src/main/java/br/com/financecontrol/exception/GlobalExceptionHandler.java:27-56`
- `backend/src/main/resources/messages.properties:22-24,44-45,56-60`
- `frontend/src/pages/CreditCardsPage.tsx:36-545`
- `frontend/src/services/creditCardService.ts:11-25`
- `frontend/src/routes/AppRoutes.tsx:29`
- `frontend/src/components/Message.tsx:17-27`
