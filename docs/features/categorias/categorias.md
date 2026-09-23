# História: Categorias

## Descrição

Permite ao usuário organizar receitas e despesas por categoria, com as operações
de criar, listar (com filtro por tipo), editar e desativar. A categoria nasce
`ACTIVE` e a desativação é lógica e **terminal** (sem reativação). **Não há
unicidade de nome** — duas categorias com o mesmo nome são permitidas.

Categoria inativa não pode ser usada em transação/compra (409) e transações de
receita exigem categoria `INCOME` (e despesa, categoria `EXPENSE`). Categorias
de outro usuário respondem **404** (ownership).

---

## API

### Endpoints

| Método | Path | Sucesso | Autenticação |
|---|---|---|---|
| POST | `/categories` | **201 Created** | sim |
| GET | `/categories` (`?type=INCOME\|EXPENSE` opcional) | **200 OK** (array ordenado por nome) | sim |
| GET | `/categories/{id}` | **200 OK** / 404 | sim |
| PUT | `/categories/{id}` | **200 OK** | sim |
| PATCH | `/categories/{id}/deactivate` | **204 No Content** (corpo vazio) | sim |
| DELETE | `*` | **não existe** (sem delete físico) | — |

### Request — `POST /categories` (igual para `PUT /categories/{id}`)

```json
{
  "name": "Alimentação",
  "type": "EXPENSE"            // INCOME | EXPENSE
}
```

| Campo | Validação JSR-303 | Mensagem |
|---|---|---|
| `name` | `@NotBlank`, `@Size(max=100)` | name.required / name.tooLong |
| `type` | `@NotNull` (`CategoryType`: INCOME\|EXPENSE) | type.required |

`CategoryRequest` e `CategoryUpdateRequest` têm exatamente os mesmos campos.

### Response — `CategoryResponse` (POST/GET/GET id/PUT)

```json
{
  "id": "<uuid>",
  "name": "Alimentação",
  "type": "EXPENSE",
  "status": "ACTIVE",
  "createdAt": "...",
  "updatedAt": "..."
}
```

`GET /categories` retorna um **array** de `CategoryResponse`. `PATCH .../deactivate`
retorna **204 sem corpo**.

### Shape de erro (compartilhado)

```json
{
  "timestamp": "...",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Categoria não encontrada",
  "path": "...",
  "fields": null
}
```

`fields` (map campo→mensagem) só é preenchido em 400 de validação. Erros já
chegam traduzidos para pt-BR (`messages.properties`).

### Status usados nesta feature

| Cenário | HTTP | `error` |
|---|---|---|
| Criação/leitura/edição com dados válidos | 201 / 200 | — |
| Desativação com dados válidos | 204 | — |
| Categoria inexistente ou de outro usuário (ownership) | 404 | `NOT_FOUND` |
| Re-desativar categoria já inativa | 409 | `BUSINESS_RULE_VIOLATION` |
| Campo obrigatório ausente / name > 100 | 400 | `VALIDATION_ERROR` (+ `fields`) |
| `type` inválido no corpo ou em `?type=` | 400 | `BAD_REQUEST` |

---

## Regras de negócio (API)

1. **Categoria nasce `ACTIVE`** (`CategoryService.java:40`).
2. **Lista ordenada por nome;** `?type=` filtra por `INCOME`/`EXPENSE`
   (`findByUser_IdAndTypeOrderByName`, opcional) (`CategoryService.java:45-54`).
3. **Ownership:** acesso via `findByUser_IdAndId`; categoria de outro usuário ou
   inexistente → 404 `category.notFound` (`CategoryService.java:79-82`).
4. **Re-desativar → 409:** categoria já `INACTIVE` recebendo novo `deactivate`
   → 409 `category.alreadyInactive` (`CategoryService.java:72-74`).
5. **Desativação terminal sem delete físico:** PATCH só vira `status INACTIVE`;
   não existe endpoint de reativação nem `DELETE`.
6. **Categoria inativa bloqueia uso:** transação nova → 409 `category.notActive`
   (`TransactionService.java:93-95`); compra no cartão → 409 (`PurchaseService.java:71-73`).
7. **Tipo incompatível com transação:** `INCOME` exige categoria `INCOME`
   (409 `category.mustBeIncome`); `EXPENSE` exige `EXPENSE`
   (409 `category.mustBeExpense`) (`TransactionService.java:96-101`).
8. **Sem unicidade de nome:** não há `@UniqueConstraint` nem validação de
   duplicidade — duas categorias com o mesmo nome coexistem.

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** um usuário autenticado
- **Quando** envio `POST /categories` com `name` e `type` válidos
- **Então** recebo 201 com `CategoryResponse` e `status` = `ACTIVE`

- **Dado** categorias de receita e despesa criadas
- **Quando** envio `GET /categories`
- **Então** recebo 200 com array ordenado por nome contendo todas

- **Dado** categorias de receita e despesa criadas
- **Quando** envio `GET /categories?type=INCOME` (ou `type=EXPENSE`)
- **Então** recebo 200 apenas com as categorias do tipo informado

- **Dado** uma categoria existente
- **Quando** envio `GET /categories/{id}`
- **Então** recebo 200 com os dados da categoria

- **Dado** uma categoria existente
- **Quando** envio `PUT /categories/{id}` com novo `name`/`type`
- **Então** recebo 200 com os dados atualizados

- **Dado** uma categoria ativa
- **Quando** envio `PATCH /categories/{id}/deactivate`
- **Então** recebo 204 e a categoria passa a `status` `INACTIVE`

### Casos negativos / borda
- **Dado** uma categoria já desativada
- **Quando** envio `PATCH /categories/{id}/deactivate` novamente
- **Então** recebo 409 (`category.alreadyInactive`)

- **Dado** `GET /categories/{id}` (ou PUT/deactivate) com id inexistente ou de
  outro usuário
- **Quando** envio a requisição
- **Então** recebo 404 `NOT_FOUND` (`category.notFound`)

- **Dado** um `POST /categories` com `name`/`type` ausentes ou `name` > 100
- **Quando** envio o cadastro
- **Então** recebo 400 `VALIDATION_ERROR` com `fields` apontando o campo

- **Dado** um `POST /categories` com `type` inválido ou `GET /categories?type=INVALIDO`
- **Quando** envio a requisição
- **Então** recebo 400 `BAD_REQUEST`

- **Dado** duas categorias com o mesmo nome
- **Quando** crio a segunda após a primeira
- **Então** ambas são criadas com 201 (não há unicidade de nome)

> Uso de categoria inativa/tipo incompatível em transação (regras 6 e 7):
> coberto por CTs da feature **transacoes** (409), reutilizando a categoria
> como pré-condição.

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/CategoriesPage.tsx`, `frontend/src/services/categoryService.ts`, `frontend/src/i18n/pt.ts:271-293`.

### Rota
- `/categorias` (`categories-page`); acesso pelo dropdown do usuário na navbar.

### Campos do formulário (`categories-form`)
| Campo | testid | Comportamento |
|---|---|---|
| Nome | `categories-form-name-input` | `required`, `maxLength=100` |
| Tipo | `categories-form-type-select` | select EXPENSE (`Despesa`, default) / INCOME (`Receita`) |

### Comportamento
- `categories-page-new-btn` abre o formulário em modo criação; edição
  (`categories-list-item-{id}-edit-btn`) preenche name/type.
- Submit → `POST` (criação) ou `PUT` (edição); sucesso exibe `Message` com
  `categories.created`/`categories.updated` e recarrega a lista.
- `categories-list-filter` (select `Filtrar por tipo`) → `GET /categories?type=`
  com `INCOME`/`EXPENSE` ou sem filtro (`allTypes`).
- `categories-list-item-{id}-deactivate-btn` (só em itens `ACTIVE`) → `PATCH`
  gera `Message` `categories.deactivated`; item inativo mostra badge e **perde**
  os botões de ação.
- Cards resumo: `categories-summary-total` (ativas), `-expense`,
  `-income` (contagem por tipo entre ativas).
- Lista vazia → `categories-list-empty`.
- Erro de API formatado por `formatApiError` e exibido no `Message`.

### data-testids relevantes
`categories-page`, `categories-page-new-btn`, `categories-form`,
`categories-form-name-input`, `categories-form-type-select`,
`categories-form-submit-btn`, `categories-form-cancel-btn`,
`categories-list-filter`, `categories-list-item-{id}`,
`categories-list-item-{id}-edit-btn`, `categories-list-item-{id}-deactivate-btn`,
`categories-list-empty`, `categories-summary-total`,
`categories-summary-expense`, `categories-summary-income`.

---

## Regras de negócio (UI)

1. **Convenção cliente = JSR-303 do backend:** nome `required`/`maxLength=100` —
   submit bloqueado pelo browser antes de chamar a API.
2. **Tipo default EXPENSE** no formulário de criação (`emptyForm.type = 'EXPENSE'`).
3. **Filtro por tipo** dispara `GET /categories?type=...`; opção em branco
   (`allTypes`) remove o parâmetro.
4. **Sucesso recarrega a lista** e exibe `Message` (`categories.created`/
   `categories.updated`/`categories.deactivated`).
5. **Desativação visível e terminal:** item inativo exibe badge e não tem mais
   os botões editar/desativar.
6. **Nomes duplicados são livres** (mesma renderização; sem bloqueio/client).

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** a tela `/categorias` com usuário autenticado
- **Quando** preencho nome e seleciono o tipo e clico em criar
- **Então** a categoria é exibida em `categories-list` com badge do tipo e `ACTIVE`

- **Dado** uma categoria na lista
- **Quando** clico em `-edit-btn`, altero nome/tipo e salvo
- **Então** o item é atualizado

- **Dado** categorias de receita e despesa na lista
- **Quando** seleciono um tipo no `categories-list-filter`
- **Então** só as categorias do tipo selecionado são exibidas

- **Dado** uma categoria ativa na lista
- **Quando** clico em `-deactivate-btn`
- **Então** o item passa a exibir `INACTIVE` e os botões de ação somem

### Casos negativos / borda
- **Dado** o formulário de criação com nome em branco
- **Quando** tento submeter
- **Então** a validação `required` bloqueia e **não** chama a API

- **Dado** nome com mais de 100 caracteres
- **Quando** tento preencher/submeter
- **Então** o `maxLength=100` limita a entrada

- **Dado** duas categorias com o mesmo nome
- **Quando** crio a segunda pela UI após a primeira
- **Então** ambas são exibidas na lista (sem erro)

---

## Referência (código-fonte — app finance-control)

- `backend/src/main/java/br/com/financecontrol/controller/CategoryController.java:39-74`
- `backend/src/main/java/br/com/financecontrol/service/CategoryService.java:32-82`
- `backend/src/main/java/br/com/financecontrol/dto/category/CategoryRequest.java:9-18` / `CategoryUpdateRequest.java:8-15` / `CategoryResponse.java:9-16`
- `backend/src/main/java/br/com/financecontrol/entity/enums/CategoryType.java`
- `backend/src/main/resources/messages.properties:25-31,44-45,55`
- `frontend/src/pages/CategoriesPage.tsx:44-92,150-205,210-299`
- `frontend/src/services/categoryService.ts:9-23`
- `frontend/src/i18n/pt.ts:271-293`