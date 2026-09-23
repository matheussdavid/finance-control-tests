# História: Contas

## Descrição

Permite que o usuário cadastre suas contas de dinheiro (conta corrente, reserva
e dinheiro físico), liste, edite e desative. Não existe `DELETE` físico: a
desativação é lógica (`status INACTIVE`) e **terminal** (não há endpoint de
reativação).

O saldo da conta nasce igual ao `initialBalance` informado na criação e só muda
por transações, transferências e pagamentos de fatura — o `PUT /accounts/{id}`
**não altera** `initialBalance` nem `balance`. Saldo pode ficar negativo. Conta
desativada deixa de receber transação/transferência/pagamento de fatura;
contas de outro usuário respondem **404** (ownership por `findByUser_IdAndId`).

---

## API

### Endpoints

| Método | Path | Sucesso | Autenticação |
|---|---|---|---|
| POST | `/accounts` | **201 Created** | sim |
| GET | `/accounts` | **200 OK** (array ordenado por nome, sem paginação) | sim |
| GET | `/accounts/{id}` | **200 OK** / 404 | sim |
| PUT | `/accounts/{id}` | **200 OK** (não altera `initialBalance`) | sim |
| PATCH | `/accounts/{id}/deactivate` | **204 No Content** (corpo vazio) | sim |
| DELETE | `*` | **não existe** (sem delete físico) | — |

### Request — `POST /accounts`

```json
{
  "name": "Nubank",
  "type": "CHECKING",          // CHECKING | SAVINGS | CASH
  "initialBalance": 1000.00
}
```

| Campo | Validação JSR-303 | Mensagem |
|---|---|---|
| `name` | `@NotBlank`, `@Size(max=100)` | name.required / name.tooLong |
| `type` | `@NotNull` (`AccountType`: CHECKING\|SAVINGS\|CASH) | type.required |
| `initialBalance` | `@NotNull`, `@DecimalMin("0.0")` | initialBalance.required / initialBalance.notNegative |

### Request — `PUT /accounts/{id}`

```json
{
  "name": "Nubank PJ",
  "type": "SAVINGS"
}
```

`AccountUpdateRequest` só tem `name` (`@NotBlank`, ≤100) e `type` (`@NotNull`).
**Não envia `initialBalance`** — saldo inicial é imutável após a criação.

### Response — `AccountResponse` (POST/GET/GET id/PUT)

```json
{
  "id": "<uuid>",
  "name": "Nubank",
  "type": "CHECKING",
  "initialBalance": 1000.00,
  "balance": 1000.00,
  "status": "ACTIVE",
  "createdAt": "...",
  "updatedAt": "..."
}
```

`GET /accounts` retorna um **array** de `AccountResponse`. `PATCH .../deactivate`
retorna **204 sem corpo**.

### Shape de erro (compartilhado)

```json
{
  "timestamp": "...",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Conta não encontrada",
  "path": "...",
  "fields": null
}
```

`fields` (map campo→mensagem) só é preenchido em 400 de validação. Erros já
chegam traduzidos para pt-BR (`messages.properties`).

### Status usados nesta feature

| Cenário | HTTP | `error` |
|---|---|---|
| Criação/listagem/busca/atualização com dados válidos | 201 / 200 | — |
| Desativação com dados válidos | 204 | — |
| Conta inexistente ou de outro usuário (ownership) | 404 | `NOT_FOUND` |
| Re-desativar conta já inativa | 409 | `BUSINESS_RULE_VIOLATION` |
| Campo obrigatório ausente / negativo / name > 100 | 400 | `VALIDATION_ERROR` (+ `fields`) |
| `type` com valor inválido / JSON malformado | 400 | `BAD_REQUEST` |

---

## Regras de negócio (API)

1. **Saldo inicial = `initialBalance`:** ao criar, `balance` nasce com o valor de
   `initialBalance` (`AccountService.java:39-40`); status nasce `ACTIVE` (:41).
2. **`PUT` não altera saldo:** o update só seta `name` e `type`;
   `initialBalance`/`balance` permanecem imutáveis (`AccountService.java:58-63`).
   **Limitação do app:** não há ajuste manual de saldo.
3. **Lista ordenada por nome:** `findByUser_IdOrderByName` (sem paginação)
   (`AccountService.java:46-50`).
4. **Ownership:** todo acesso usa `findByUser_IdAndId(userId, id)`; conta de
   outro usuário (ou inexistente) → 404 `account.notFound` (`AccountService.java:75-78`).
5. **Desativação lógica e terminal:** `PATCH` zera (`status INACTIVE`, sem
   delete físico); re-desativar → 409 `account.alreadyInactive`
   (`AccountService.java:66-73`). **Não existe endpoint de reativação** — estado
   INACTIVE é terminal (enum `Status`: ACTIVE|INACTIVE).
6. **Conta inativa bloqueia movimentação:** transação nova, transferência e
   pagamento de fatura respondem 409 (`account.notActive`) — regra implementada
   nos services de cada fluxo (`TransactionService.java:90-92`,
   `TransferService.java:82-87`, `InvoiceService.java:81-83`).
7. **Saldo pode negativar:** não há validação de cobertura; DB não tem CHECK em
   `balance` (`V1__init.sql`).
8. **Sem delete físico:** nenhum `@DeleteMapping` existe no app.

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** um usuário autenticado
- **Quando** envio `POST /accounts` com `name`, `type` e `initialBalance` válidos
- **Então** recebo 201 com `AccountResponse` contendo `balance` = `initialBalance`
  e `status` = `ACTIVE`

- **Dado** uma conta recém-criada
- **Quando** envio `GET /accounts`
- **Então** recebo 200 com array contendo a conta, ordenado por `name`

- **Dado** uma conta existente do usuário
- **Quando** envio `GET /accounts/{id}`
- **Então** recebo 200 com os dados da conta

- **Dado** uma conta existente
- **Quando** envio `PUT /accounts/{id}` com novo `name`/`type`
- **Então** recebo 200 e `initialBalance`/`balance` **não** mudam

- **Dado** uma conta ativa
- **Quando** envio `PATCH /accounts/{id}/deactivate`
- **Então** recebo 204 e a conta passa a `status` `INACTIVE`

### Casos negativos / borda
- **Dado** uma conta já desativada
- **Quando** envio `PATCH /accounts/{id}/deactivate` novamente
- **Então** recebo 409 (`account.alreadyInactive`)

- **Dado** `GET /accounts/{id}` (ou PUT/deactivate) com id inexistente ou de
  outro usuário
- **Quando** envio a requisição
- **Então** recebo 404 `NOT_FOUND` (`account.notFound`)

- **Dado** um `POST /accounts` com `name`/`type`/`initialBalance` ausentes,
  `initialBalance` negativo ou `name` > 100
- **Quando** envio o cadastro
- **Então** recebo 400 `VALIDATION_ERROR` com `fields` apontando o campo

- **Dado** um `POST /accounts` com `type` de valor inválido
- **Quando** envio o cadastro
- **Então** recebo 400 `BAD_REQUEST`

- **Dado** uma conta com saldo insuficiente para uma despesa
- **Quando** crio a transação EXPENSE
- **Então** o `balance` da conta fica **negativo** (sem bloqueio)

> Bloqueio de movimentação em conta inativa (regra 6): coberto por CTs da
> feature **transacoes** (transação em conta inativa → 409), reutilizando a
> conta como pré-condição.

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/AccountsPage.tsx`, `frontend/src/services/accountService.ts`, `frontend/src/i18n/pt.ts:295-317`.

### Rota
- `/contas` (`accounts-page`); acesso pelo dropdown do usuário na navbar.

### Campos do formulário (`accounts-form`)
| Campo | testid | Comportamento |
|---|---|---|
| Nome | `accounts-form-name-input` | `required`, `maxLength=100` |
| Tipo | `accounts-form-type-select` | select CHECKING (`Conta corrente`) / SAVINGS (`Reservas`) / CASH (`Dinheiro`) |
| Saldo inicial | `accounts-form-balance-input` | `type=number`, `step=0.01`, `min=0`, `required`; **só aparece no modo criação** |

### Comportamento
- `accounts-page-new-btn` abre o formulário em modo criação; no modo edição o
  form preenche name/type e **oculta** o campo de saldo (`{!editingId && ...}`).
- Submit → `POST` (criação) ou `PUT` (edição); sucesso exibe `Message` com
  `accounts.created`/`accounts.updated` e recarrega a lista.
- `accounts-list-item-{id}-deactivate-btn` (só em itens `ACTIVE`) → `PATCH`
  gera `Message` `accounts.deactivated`; item inativo mostra badge e **perde**
  os botões de ação.
- `accounts-summary-card` exibe `accounts.totalActiveBalance` (soma do
  `balance` apenas das contas `ACTIVE`).
- Lista vazia → `accounts-list-empty` (`accounts.emptyTitle`/`accounts.emptyDesc`).
- Erro de API formatado por `formatApiError` e exibido no `Message`.

### data-testids relevantes
`accounts-page`, `accounts-page-header`, `accounts-page-new-btn`,
`accounts-summary-card`, `accounts-form`, `accounts-form-name-input`,
`accounts-form-type-select`, `accounts-form-balance-input`,
`accounts-form-submit-btn`, `accounts-form-cancel-btn`, `accounts-list`,
`accounts-list-item-{id}`, `accounts-list-item-{id}-edit-btn`,
`accounts-list-item-{id}-deactivate-btn`, `accounts-list-empty`.

---

## Regras de negócio (UI)

1. **Convenção cliente = JSR-303 do backend:** nome `required`/`maxLength=100`,
   saldo `required`/`min=0` — o submit é bloqueado pelo browser antes de
   chamar a API (HTML/React `required`).
2. **Modo edição não edita saldo:** `accounts-form-balance-input` só é
   renderizado quando `!editingId`; o PUT não envia `initialBalance`.
3. **Sucesso recarrega a lista** e exibe `Message` (`accounts.created`/
   `accounts.updated`/`accounts.deactivated`).
4. **Desativação é visível e terminal na UI:** item inativo exibe badge
   `statusLabels[INACTIVE]` e não tem mais os botões editar/desativar.
5. **Resumo soma apenas contas ativas** (`activeAccounts.filter(status===ACTIVE)`).
6. **Erro de API** é exibido no `Message` (`formatApiError`), sem navegação.

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** a tela `/contas` com usuário autenticado
- **Quando** preencho nome, seleciono o tipo e informo saldo inicial válidos e
  clico em criar
- **Então** a conta é exibida em `accounts-list` com saldo e badge `ACTIVE`

- **Dado** uma conta na lista
- **Quando** clico em `-edit-btn`, altero nome/tipo e salvo
- **Então** o item é atualizado e o campo de saldo não aparece no formulário

- **Dado** uma conta ativa na lista
- **Quando** clico em `-deactivate-btn`
- **Então** o item passa a exibir `INACTIVE` e os botões de ação somem

### Casos negativos / borda
- **Dado** o formulário de criação com nome em branco
- **Quando** tento submeter
- **Então** a validação `required` bloqueia e **não** chama a API

- **Dado** saldo inicial em branco ou negativo
- **Quando** tento submeter
- **Então** o `input` (`required`/`min=0`) bloqueia e não chama a API

- **Dado** nome com mais de 100 caracteres
- **Quando** tento preencher/submeter
- **Então** o `maxLength=100` limita a entrada

---

## Referência (código-fonte — app finance-control)

- `backend/src/main/java/br/com/financecontrol/controller/AccountController.java:37-71`
- `backend/src/main/java/br/com/financecontrol/service/AccountService.java:31-78`
- `backend/src/main/java/br/com/financecontrol/dto/account/AccountRequest.java:12-26` / `AccountUpdateRequest.java:8-15` / `AccountResponse.java:10-19`
- `backend/src/main/java/br/com/financecontrol/entity/enums/AccountType.java` / `Status.java`
- `backend/src/main/resources/messages.properties:13-15,44-45,61-62`
- `frontend/src/pages/AccountsPage.tsx:58-100,148-227,256-309`
- `frontend/src/services/accountService.ts:10-23`
- `frontend/src/i18n/pt.ts:295-317`