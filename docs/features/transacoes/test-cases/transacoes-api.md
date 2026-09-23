# Casos de Teste API - Transações

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/transacoes/transacoes.md

## Resumo Executivo
- **Total de casos de teste:** 29
- **Executados:** 1/29 (3%) — persistência DB (`TransactionPersistenceTest`)
- **Distribuição por prioridade:**
  - P0: 9 (6 contratos + 3 fluxo principal)
  - P1: 11
  - P2: 5
  - P3: 4 (1 caso + 3 notas `[-]`)
- **Critérios de aceite cobertos:** 15 de 15 (API) — todos os CAs do `transacoes.md`
- **Regras de negócio cobertas:** 7/9 direta — regras 7 (pag. de fatura) e 5
  (sem edit/remove) documentadas como casos `[-]`
- **Observações gerais:** único CT executado é o de persistência PostgreSQL
  (`[x]`, CT-009). 5 dos 6 contratos dependem de schema ainda não criado.
  Revisão: nenhum CT removido/combinado; 1 repriorização (data futura P2→P3).

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Contrato do response de sucesso do POST /transactions | P0 | Contrato | - | - |
| [ ] | CT-002 | Contrato do response de erro do POST /transactions | P0 | Contrato | - | - |
| [ ] | CT-003 | Contrato do response de sucesso do GET /transactions | P0 | Contrato | - | - |
| [ ] | CT-004 | Contrato do response de erro do GET /transactions | P0 | Contrato | - | - |
| [ ] | CT-005 | Contrato do response de sucesso do GET /transactions/{id} | P0 | Contrato | - | - |
| [ ] | CT-006 | Contrato do response de erro do GET /transactions/{id} | P0 | Contrato | - | - |
| [ ] | CT-007 | Criar despesa retorna 201 e subtrai o saldo da conta | P0 | Positivo | CA-expense | #1 |
| [ ] | CT-008 | Criar receita retorna 201 e soma o saldo da conta | P0 | Positivo | CA-income | #1 |
| [x] | CT-009 | Despesa criada via API persiste no PostgreSQL com dados corretos | P0 | Positivo | CA-persistencia | #1 |
| [ ] | CT-010 | Listar transacoes retorna Page ordenada por transactionDate DESC | P1 | Positivo | CA-listar-filtros | #8 |
| [ ] | CT-011 | Buscar transacao por id retorna a transacao | P1 | Positivo | CA-buscar | #6 |
| [ ] | CT-012 | Descricao opcional ausente e aceita | P1 | Positivo | CA-expense | #1 |
| [ ] | CT-013 | Transacao em conta inativa retorna 409 | P1 | Negativo | CA-conta-inativa | #2 |
| [ ] | CT-014 | Transacao com categoria inativa retorna 409 | P1 | Negativo | CA-categoria-inativa | #3 |
| [ ] | CT-015 | Receita com categoria EXPENSE retorna 409 | P1 | Negativo | CA-tipo-incompativel | #4 |
| [ ] | CT-016 | Despesa com categoria INCOME retorna 409 | P1 | Negativo | CA-tipo-incompativel | #4 |
| [ ] | CT-017 | Campos obrigatorios ausentes retornam 400 VALIDATION_ERROR | P1 | Negativo | CA-campos-ausentes | #1 |
| [ ] | CT-018 | Amount menor que 0.01 retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-campos-ausentes | #1 |
| [ ] | CT-019 | Conta ou categoria de outro usuario retorna 404 | P1 | Negativo | CA-ownership | #6 |
| [ ] | CT-020 | Filtros combinados limitam a Page de transacoes | P1 | Positivo | CA-listar-filtros | #8 |
| [ ] | CT-021 | Paginacao page/size funciona com sort DESC | P2 | Positivo | CA-listar-filtros | #8 |
| [ ] | CT-022 | Type invalido no corpo retorna 400 BAD_REQUEST | P2 | Negativo | CA-400 | #1 |
| [ ] | CT-023 | Filtro type=TRANSFER retorna 400 BAD_REQUEST (bug conhecido) | P2 | Negativo | CA-400 | #8 |
| [ ] | CT-024 | Data fora do formato yyyy-MM-dd retorna 400 BAD_REQUEST | P2 | Negativo | CA-400 | #9 |
| [ ] | CT-025 | Buscar transacao inexistente retorna 404 | P2 | Negativo | CA-buscar | #6 |
| [ ] | CT-026 | Transacao com data futura e aceita pelo backend | P3 | Positivo | CA-400 | #9 |
| [-] | CT-027 | Sem update de transacao (nao existe PUT/PATCH) | P3 | Normal | - | #5 |
| [-] | CT-028 | Sem delete de transacao (nao existe DELETE) | P3 | Normal | - | #5 |
| [-] | CT-029 | Pagamento de fatura nao cria Transaction | P3 | Normal | CA-pagamento-fatura | #7 |

---

### CT-001 - Contrato do response de sucesso do POST /transactions
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/transacoes/transaction-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com conta e categoria EXPENSE (`FinanceFixture`)
- **Passos:**
  1. Enviar `POST /transactions` com payload válido
  2. Validar o corpo (201) contra o schema estrito
- **Dados de entrada:** `TransactionRequest` faker (type, description, amount, accountId, categoryId, transactionDate)
- **Resultado esperado:** HTTP 201; body `{id,type,description,amount,accountId,accountName,categoryId,categoryName,transactionDate,createdAt}` conforme schema (`additionalProperties: false`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-002 - Contrato do response de erro do POST /transactions
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /transactions` sem `amount` (400 de validação)
  2. Validar o corpo do erro contra o schema
- **Dados de entrada:** payload sem `amount`
- **Resultado esperado:** HTTP 400; body `{timestamp,status,error,message,path,fields}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-003 - Contrato do response de sucesso do GET /transactions
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/transacoes/transaction-page-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com ao menos 1 transação
- **Passos:**
  1. Enviar `GET /transactions`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** sem filtros
- **Resultado esperado:** HTTP 200; body é `Page` com metadados Spring Data (`content/totalElements/totalPages/number/size`) e `content[]` de `TransactionResponse`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas; shape `Page` documentado no inventário

### CT-004 - Contrato do response de erro do GET /transactions
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /transactions` sem `Authorization`
  2. Validar o corpo do erro (401) contra o schema
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401; body `{timestamp,status,error,message,path}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-005 - Contrato do response de sucesso do GET /transactions/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/transacoes/transaction-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com 1 transação
- **Passos:**
  1. Enviar `GET /transactions/{id}` com o id da transação
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** id de transação recém-criada
- **Resultado esperado:** HTTP 200; body conforme `schemas/transacoes/transaction-response.json`
- **CA:** - **Regra:** - **Observacoes:** shape idêntico ao do POST (mesmo DTO); mantido por endpoint conforme convenção de contrato

### CT-006 - Contrato do response de erro do GET /transactions/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /transactions/{uuid-aleatorio}` (inexistente)
  2. Validar o corpo do erro (404) contra o schema
- **Dados de entrada:** UUID aleatório
- **Resultado esperado:** HTTP 404; body `{timestamp,status,error,message,path}` conforme schema (`message` = "Transação não encontrada")
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-007 - Criar despesa retorna 201 e subtrai o saldo da conta
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** conta (saldo 1000.00) e categoria EXPENSE via `FinanceFixture`
- **Passos:**
  1. Enviar `POST /transactions` com `type=EXPENSE`, `amount=200.00`, conta/categoria válidas
  2. Asserir 201 e buscar a conta
- **Dados de entrada:** EXPENSE de 200.00
- **Resultado esperado:** HTTP 201; `TransactionResponse` correto; `GET /accounts/{id}` retorna `balance` 800.00
- **CA:** CA-expense - **Regra:** #1
- **Observacoes:** saldo subtraído via `adjustBalance` (`TransactionService.java:104-109`)

### CT-008 - Criar receita retorna 201 e soma o saldo da conta
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** conta (saldo 1000.00) e categoria INCOME via fixtures
- **Passos:**
  1. Enviar `POST /transactions` com `type=INCOME`, `amount=500.00`, conta/categoria válidas
  2. Asserir 201 e buscar a conta
- **Dados de entrada:** INCOME de 500.00
- **Resultado esperado:** HTTP 201; `GET /accounts/{id}` retorna `balance` 1500.00
- **CA:** CA-income - **Regra:** #1
- **Observacoes:** categoria INCOME é pré-condição (regra 4)

### CT-009 - Despesa criada via API persiste no PostgreSQL com dados corretos
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API + DB
- **Pre-condicoes:** contexto financeiro (`FinanceFixture#createExpenseContext`)
- **Passos:**
  1. Criar transação EXPENSE via `TransactionClient`
  2. Consultar o PostgreSQL por `findByUserAndDescription`
  3. Asserir `type`, `amount`, `transactionDate`, `accountName`, `categoryName`
- **Dados de entrada:** description faker única, `amount` faker, data de hoje
- **Resultado esperado:** registro existe no banco com os dados corretos (persistência real)
- **CA:** CA-persistencia - **Regra:** #1
- **Observacoes:** **[EXECUTADO]** — `TransactionPersistenceTest#devePersistirDespesaCriadaViaApi` (`@Tag("api")`); camada API+DB (`TransactionRepository`)

### CT-010 - Listar transacoes retorna Page ordenada por transactionDate DESC
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** transações em datas distintas (ex.: hoje e ontem)
- **Passos:**
  1. Enviar `GET /transactions` (sem filtros)
  2. Asserir 200, `content[]` e ordem
- **Dados de entrada:** nenhum filtro
- **Resultado esperado:** HTTP 200; `Page` com as transações ordenadas por `transactionDate DESC`
- **CA:** CA-listar-filtros - **Regra:** #8
- **Observacoes:** sort default do `@PageableDefault`

### CT-011 - Buscar transacao por id retorna a transacao
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** 1 transação criada via API
- **Passos:**
  1. Enviar `GET /transactions/{id}`
  2. Asserir 200 e campos
- **Dados de entrada:** id da transação criada
- **Resultado esperado:** HTTP 200; `id`/`type`/`amount` conforme a criação
- **CA:** CA-buscar - **Regra:** #6
- **Observacoes:** -

### CT-012 - Descricao opcional ausente e aceita
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** conta e categoria EXPENSE via fixtures
- **Passos:**
  1. Enviar `POST /transactions` sem `description`
  2. Asserir 201
- **Dados de entrada:** payload sem `description`
- **Resultado esperado:** HTTP 201; `description` nula no response
- **CA:** CA-expense - **Regra:** #1
- **Observacoes:** campo opcional (`@Size(max=255)` apenas)

### CT-013 - Transacao em conta inativa retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** conta desativada via API (`PATCH /accounts/{id}/deactivate`) + categoria EXPENSE ativa
- **Passos:**
  1. Enviar `POST /transactions` usando a conta inativa
  2. Asserir status e mensagem
- **Dados de entrada:** accountId de conta INACTIVE
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` ("A conta está inativa" — `account.notActive`)
- **CA:** CA-conta-inativa - **Regra:** #2
- **Observacoes:** fechamento da regra **contas** (regra 6 do `contas.md`) — teste consolidado aqui por dedupe

### CT-014 - Transacao com categoria inativa retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** categoria desativada via API + conta ativa
- **Passos:**
  1. Enviar `POST /transactions` usando a categoria inativa
  2. Asserir status e mensagem
- **Dados de entrada:** categoryId de categoria INACTIVE
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` ("A categoria está inativa" — `category.notActive`)
- **CA:** CA-categoria-inativa - **Regra:** #3
- **Observacoes:** fechamento da regra 6 do `categorias.md` — teste consolidado aqui por dedupe

### CT-015 - Receita com categoria EXPENSE retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** conta ativa e categoria EXPENSE
- **Passos:**
  1. Enviar `POST /transactions` com `type=INCOME` e a categoria EXPENSE
  2. Asserir 409
- **Dados de entrada:** INCOME + categoria EXPENSE
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` ("A categoria deve ser de receita para transações de entrada" — `category.mustBeIncome`)
- **CA:** CA-tipo-incompativel - **Regra:** #4
- **Observacoes:** -

### CT-016 - Despesa com categoria INCOME retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** conta ativa e categoria INCOME
- **Passos:**
  1. Enviar `POST /transactions` com `type=EXPENSE` e a categoria INCOME
  2. Asserir 409
- **Dados de entrada:** EXPENSE + categoria INCOME
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` ("A categoria deve ser de despesa para transações de saída" — `category.mustBeExpense`)
- **CA:** CA-tipo-incompativel - **Regra:** #4
- **Observacoes:** -

### CT-017 - Campos obrigatorios ausentes retornam 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /transactions` omitindo um campo obrigatório por vez (type, amount, accountId, categoryId, transactionDate)
  2. Asserir 400 e `fields`
- **Dados de entrada:** payloads parametrizados (campo ausente)
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.<campo>` preenchido (`type.required`/`amount.required`/`accountId.required`/`categoryId.required`/`transactionDate.required`)
- **CA:** CA-campos-ausentes - **Regra:** #1
- **Observacoes:** caso parametrizado (`deveRejeitarTransacaoSemCampoObrigatorio`)

### CT-018 - Amount menor que 0.01 retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** conta e categoria válidas (fixtures)
- **Passos:**
  1. Enviar `POST /transactions` com `amount=0` (ou 0.001)
  2. Asserir 400 e `fields.amount`
- **Dados de entrada:** `amount: 0`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`amount.greaterThanZero` = "O valor deve ser maior que 0")
- **CA:** CA-campos-ausentes - **Regra:** #1
- **Observacoes:** `@DecimalMin("0.01")`; DB também tem CHECK `amount > 0`

### CT-019 - Conta ou categoria de outro usuario retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuários A (conta/categoria) e B
- **Passos:**
  1. Logar como B e enviar `POST /transactions` com accountId/categoryId de A
  2. Asserir 404
- **Dados de entrada:** accountId de A no token de B
- **Resultado esperado:** HTTP 404 `NOT_FOUND` (`account.notFound`/`category.notFound` — nunca 403)
- **CA:** CA-ownership - **Regra:** #6
- **Observacoes:** - 

### CT-020 - Filtros combinados limitam a Page de transacoes
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** transações variadas (tipos, contas, categorias, datas)
- **Passos:**
  1. Enviar `GET /transactions?type=&accountId=&categoryId=&startDate=&endDate=` combinados
  2. Asserir 200 e conteúdo
- **Dados de entrada:** combinação de todos os filtros opcionais
- **Resultado esperado:** `content[]` apenas com as transações que casam com todos os filtros
- **CA:** CA-listar-filtros - **Regra:** #8
- **Observacoes:** `findFiltered` aplica filtragem condicional

### CT-021 - Paginacao page/size funciona com sort DESC
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** mais transações que o tamanho da página (ex.: 5, size 2)
- **Passos:**
  1. Enviar `GET /transactions?page=0&size=2`
  2. Enviar `GET /transactions?page=1&size=2`
  3. Asserir `number`, `totalElements`, `content` e ordem
- **Dados de entrada:** page/size
- **Resultado esperado:** paginação consistente (sem sobreposição), `transactionDate DESC` preservado
- **CA:** CA-listar-filtros - **Regra:** #8
- **Observacoes:** `size` não tem limitação default explícita no controller (front usa 50)

### CT-022 - Type invalido no corpo retorna 400 BAD_REQUEST
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /transactions` com `type: "TRANSFER"`
  2. Asserir 400
- **Dados de entrada:** enum inválido no corpo
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (enum mismatch — sem `fields`)
- **CA:** CA-400 - **Regra:** #1
- **Observacoes:** `TransactionType` só aceita INCOME|EXPENSE

### CT-023 - Filtro type=TRANSFER retorna 400 BAD_REQUEST (bug conhecido)
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /transactions?type=TRANSFER`
  2. Asserir 400
- **Dados de entrada:** query param `type=TRANSFER`
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (enum mismatch)
- **CA:** CA-400 - **Regra:** #8
- **Observacoes:** bug conhecido documentado no inventário — a UI de /gastos oferece a opção no select mas o backend rejeita (relacionado ao CT-009 UI)

### CT-024 - Data fora do formato yyyy-MM-dd retorna 400 BAD_REQUEST
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /transactions?startDate=10/09/2026`
  2. Asserir 400
- **Dados de entrada:** data em formato dd/MM/yyyy
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (`@DateTimeFormat(ISO.DATE)` — `MethodArgumentTypeMismatchException`)
- **CA:** CA-400 - **Regra:** #9
- **Observacoes:** também vale para `endDate` e `transactionDate` do corpo se enviado como string errada

### CT-025 - Buscar transacao inexistente retorna 404
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /transactions/{uuid-aleatorio}`
  2. Asserir 404
- **Dados de entrada:** UUID aleatório
- **Resultado esperado:** HTTP 404 `NOT_FOUND` ("Transação não encontrada")
- **CA:** CA-buscar - **Regra:** #6
- **Observacoes:** -

### CT-026 - Transacao com data futura e aceita pelo backend
- **Prioridade:** P3
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** conta e categoria EXPENSE ativas
- **Passos:**
  1. Enviar `POST /transactions` com `transactionDate` futura (+30 dias)
  2. Asserir 201
- **Dados de entrada:** data futura em `yyyy-MM-dd`
- **Resultado esperado:** HTTP 201 (backend **não** bloqueia data futura — só o QuickCapture do front limita `max=today()`)
- **CA:** CA-400 - **Regra:** #9
- **Observacoes:** repriorizado de P2→P3 na revisão (documenta limitação, não é regra de negócio crítica)

### CT-027 - Sem update de transacao (nao existe PUT/PATCH)
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Tentar `PUT /transactions/{id}` (ou PATCH)
  2. Observar resposta
- **Dados de entrada:** id de transação
- **Resultado esperado:** sem endpoint — 404/405 (fluxo não automatizável via API)
- **CA:** - **Regra:** #5
- **Observacoes:** [-]: não automatizável — não existe update para transaction (inventário: "Sem PUT/DELETE")

### CT-028 - Sem delete de transacao (nao existe DELETE)
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Tentar `DELETE /transactions/{id}`
  2. Observar resposta
- **Dados de entrada:** id de transação
- **Resultado esperado:** sem endpoint — 404/405 (fluxo não automatizável via API)
- **CA:** - **Regra:** #5
- **Observacoes:** [-]: não automatizável — transaction é somente criação+leitura

### CT-029 - Pagamento de fatura nao cria Transaction
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** API
- **Pre-condicoes:** (fora do escopo desta bateria) cartão, compra parcelada e fatura CLOSED
- **Passos:**
  1. Executar `POST /invoices/{id}/pay`
  2. Verificar ausência de nova linha em `/transactions` e o débito apenas no `balance`
- **Dados de entrada:** fluxo cartões/faturas
- **Resultado esperado:** o pagamento debita `balance` sem criar `Transaction` (`InvoiceService.java:85-94`)
- **CA:** CA-pagamento-fatura - **Regra:** #7
- **Observacoes:** [-]: automatizável porém exige setup de cartão/compra/fatura (features cartões/faturas) — fora desta bateria; documentado como regra/limitação

---