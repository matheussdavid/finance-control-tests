# Casos de Teste API - Contas

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/contas/contas.md

## Resumo Executivo
- **Total de casos de teste:** 26
- **Executados:** 0/26 (0%)
- **Distribuição por prioridade:**
  - P0: 12 (10 contratos + 2 fluxo principal)
  - P1: 8
  - P2: 4
  - P3: 2 (notas `[-]`)
- **Critérios de aceite cobertos:** 13 de 13 (API) — todos os CAs do `contas.md`
- **Regras de negócio cobertas:** 7/8 direta — regra 6 (conta inativa bloqueia
  movimentação) coberta pelos CTs da feature **transacoes** (409), por dedupe
- **Observações gerais:** 9 dos 10 contratos dependem de schema ainda não criado
  (`schemas/contas/*`, `schemas/common/error-response.json`); contrato do
  deactivate (204) valida status, sem corpo. Revisão: removido CT duplicado
  "endpoint autenticado sem token → 401" (já coberto na feature autenticacao).

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Contrato do response de sucesso do POST /accounts | P0 | Contrato | - | - |
| [ ] | CT-002 | Contrato do response de erro do POST /accounts | P0 | Contrato | - | - |
| [ ] | CT-003 | Contrato do response de sucesso do GET /accounts | P0 | Contrato | - | - |
| [ ] | CT-004 | Contrato do response de erro do GET /accounts | P0 | Contrato | - | - |
| [ ] | CT-005 | Contrato do response de sucesso do GET /accounts/{id} | P0 | Contrato | - | - |
| [ ] | CT-006 | Contrato do response de erro do GET /accounts/{id} | P0 | Contrato | - | - |
| [ ] | CT-007 | Contrato do response de sucesso do PUT /accounts/{id} | P0 | Contrato | - | - |
| [ ] | CT-008 | Contrato do response de erro do PUT /accounts/{id} | P0 | Contrato | - | - |
| [ ] | CT-009 | Contrato do response de sucesso do PATCH /accounts/{id}/deactivate | P0 | Contrato | - | - |
| [ ] | CT-010 | Contrato do response de erro do PATCH /accounts/{id}/deactivate | P0 | Contrato | - | - |
| [ ] | CT-011 | Criar conta com dados validos retorna 201 com saldo inicial e ACTIVE | P0 | Positivo | CA-criar-conta | #1 |
| [ ] | CT-012 | Listar contas retorna array ordenado por nome | P0 | Positivo | CA-listar | #3 |
| [ ] | CT-013 | Buscar conta por id retorna a conta | P1 | Positivo | CA-buscar | #4 |
| [ ] | CT-014 | Atualizar conta altera nome/tipo e preserva initialBalance e balance | P1 | Positivo | CA-atualizar | #2 |
| [ ] | CT-015 | Desativar conta retorna 204 e o status muda para INACTIVE | P1 | Positivo | CA-desativar | #5 |
| [ ] | CT-016 | Desativar conta ja inativa retorna 409 | P1 | Negativo | CA-re-desativar | #5 |
| [ ] | CT-017 | Buscar conta inexistente retorna 404 NOT_FOUND | P1 | Negativo | CA-404-inexistente | #4 |
| [ ] | CT-018 | Buscar conta de outro usuario retorna 404 NOT_FOUND | P1 | Negativo | CA-ownership | #4 |
| [ ] | CT-019 | Cadastro com campos obrigatorios ausentes retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-campos-ausentes | #8 |
| [ ] | CT-020 | Cadastro com saldo inicial negativo retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-saldo-negativo-criar | #8 |
| [ ] | CT-021 | Cadastro com nome maior que 100 caracteres retorna 400 | P2 | Negativo | CA-name-too-long | #8 |
| [ ] | CT-022 | Cadastro com tipo invalido retorna 400 BAD_REQUEST | P2 | Negativo | CA-type-invalido | #8 |
| [ ] | CT-023 | Atualizacao com nome/tipo ausentes retorna 400 VALIDATION_ERROR | P2 | Negativo | CA-campos-ausentes | #8 |
| [ ] | CT-024 | Saldo da conta pode ficar negativo apos despesa maior que o saldo | P2 | Positivo | CA-saldo-negativo | #7 |
| [-] | CT-025 | Sem delete fisico de conta (endpoint nao existe) | P3 | Normal | - | #9 |
| [-] | CT-026 | Desativacao e terminal (sem endpoint de reativacao) | P3 | Normal | - | #5 |

---

### CT-001 - Contrato do response de sucesso do POST /accounts
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/contas/account-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado via API (`TestUserFixture`)
- **Passos:**
  1. Enviar `POST /accounts` com payload válido e único
  2. Validar o corpo (201) contra o schema estrito
- **Dados de entrada:** `AccountRequest` faker (name, type, initialBalance)
- **Resultado esperado:** HTTP 201; body `{id,name,type,initialBalance,balance,status,createdAt,updatedAt}` conforme schema (`additionalProperties: false`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-002 - Contrato do response de erro do POST /accounts
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /accounts` com `type` de valor inválido (400)
  2. Validar o corpo do erro contra o schema
- **Dados de entrada:** `{"name":"x","type":"INVESTIMENTO","initialBalance":10}`
- **Resultado esperado:** HTTP 400; body `{timestamp,status,error,message,path,fields}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-003 - Contrato do response de sucesso do GET /accounts
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/contas/account-list-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com ao menos 1 conta
- **Passos:**
  1. Enviar `GET /accounts`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** sem query params
- **Resultado esperado:** HTTP 200; body é um array de `AccountResponse` ordenado por nome
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-004 - Contrato do response de erro do GET /accounts
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /accounts` sem `Authorization`
  2. Validar o corpo do erro (401) contra o schema
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401; body `{timestamp,status,error,message,path}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-005 - Contrato do response de sucesso do GET /accounts/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/contas/account-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com 1 conta
- **Passos:**
  1. Enviar `GET /accounts/{id}` com o id da conta
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** id de conta recém-criada
- **Resultado esperado:** HTTP 200; body conforme `schemas/contas/account-response.json`
- **CA:** - **Regra:** - **Observacoes:** shape idêntico ao do POST (mesmo DTO); mantido por endpoint conforme convenção de contrato

### CT-006 - Contrato do response de erro do GET /accounts/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /accounts/{uuid-aleatorio}` (inexistente)
  2. Validar o corpo do erro (404) contra o schema
- **Dados de entrada:** UUID aleatório
- **Resultado esperado:** HTTP 404; body `{timestamp,status,error,message,path}` conforme schema (`message` = "Conta não encontrada")
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-007 - Contrato do response de sucesso do PUT /accounts/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/contas/account-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com 1 conta
- **Passos:**
  1. Enviar `PUT /accounts/{id}` com novo `name`/`type`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** `AccountUpdateRequest` válido
- **Resultado esperado:** HTTP 200; body conforme schema com `initialBalance`/`balance` preservados
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-008 - Contrato do response de erro do PUT /accounts/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `PUT /accounts/{uuid-aleatorio}` com payload sem `type`
  2. Validar o corpo do erro contra o schema
- **Dados de entrada:** body sem campo `type`
- **Resultado esperado:** HTTP 400; body conforme schema com `fields.type` (ou 404 quando o id não existe)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-009 - Contrato do response de sucesso do PATCH /accounts/{id}/deactivate
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** n/a (sem corpo — validação de status 204)
- **Pre-condicoes:** usuário autenticado com 1 conta ativa
- **Passos:**
  1. Enviar `PATCH /accounts/{id}/deactivate`
  2. Validar HTTP 204 e corpo vazio
- **Dados de entrada:** id de conta ativa
- **Resultado esperado:** HTTP 204 No Content; body vazio
- **CA:** - **Regra:** - **Observacoes:** sem schema aplicável (response sem corpo); validar status/headers

### CT-010 - Contrato do response de erro do PATCH /accounts/{id}/deactivate
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** uma conta já desativada
- **Passos:**
  1. Reenviar `PATCH /accounts/{id}/deactivate` na conta inativa
  2. Validar o corpo do erro (409) contra o schema
- **Dados de entrada:** id de conta INACTIVE
- **Resultado esperado:** HTTP 409; body `{timestamp,status,error,message,path}` conforme schema (`message` = "A conta já está inativa")
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-011 - Criar conta com dados validos retorna 201 com saldo inicial e ACTIVE
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário registrado via API (`TestUserFixture`)
- **Passos:**
  1. Enviar `POST /accounts` com `name`/`type`/`initialBalance` válidos
  2. Asserir 201 e campos do `AccountResponse`
- **Dados de entrada:** conta CHECKING com saldo 1000.00
- **Resultado esperado:** HTTP 201; `balance` = `initialBalance`; `status` ACTIVE; `id` retornado
- **CA:** CA-criar-conta - **Regra:** #1
- **Observacoes:** usar `UserFaker.accountName()` (massa única)

### CT-012 - Listar contas retorna array ordenado por nome
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com 3 contas criadas (nomes fora de ordem no POST)
- **Passos:**
  1. Enviar `GET /accounts`
  2. Asserir 200 e ordem dos `name`
- **Dados de entrada:** contas "C","A","B" criadas em sequência
- **Resultado esperado:** HTTP 200; array com names em ordem alfabética (A,B,C) — sem paginação
- **CA:** CA-listar - **Regra:** #3
- **Observacoes:** [SUPOSICAO] somente as contas do usuário logado

### CT-013 - Buscar conta por id retorna a conta
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com 1 conta criada
- **Passos:**
  1. Enviar `GET /accounts/{id}`
  2. Asserir 200 e campos
- **Dados de entrada:** id da conta criada
- **Resultado esperado:** HTTP 200; `id`/`name`/`balance` conforme a criação
- **CA:** CA-buscar - **Regra:** #4
- **Observacoes:** -

### CT-014 - Atualizar conta altera nome/tipo e preserva initialBalance e balance
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com conta criada com saldo 1000.00
- **Passos:**
  1. Enviar `PUT /accounts/{id}` com novo `name` e `type` SAVINGS
  2. Asserir 200 e buscar a conta de novo
- **Dados de entrada:** `{"name":"<novo>","type":"SAVINGS"}` (sem `initialBalance`)
- **Resultado esperado:** HTTP 200; `name`/`type` atualizados mas `initialBalance` e `balance` intactos (1000.00)
- **CA:** CA-atualizar - **Regra:** #2
- **Observacoes:** PUT não aceita/ignora `initialBalance` no DTO

### CT-015 - Desativar conta retorna 204 e o status muda para INACTIVE
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com conta ativa
- **Passos:**
  1. Enviar `PATCH /accounts/{id}/deactivate`
  2. Asserir 204 e buscar a conta
- **Dados de entrada:** id de conta ACTIVE
- **Resultado esperado:** HTTP 204 sem corpo; `GET /accounts/{id}` retorna `status` INACTIVE
- **CA:** CA-desativar - **Regra:** #5
- **Observacoes:** -

### CT-016 - Desativar conta ja inativa retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** conta previamente desativada via API
- **Passos:**
  1. Reenviar `PATCH /accounts/{id}/deactivate`
  2. Asserir status e mensagem
- **Dados de entrada:** id de conta INACTIVE
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` ("A conta já está inativa" — `account.alreadyInactive`)
- **CA:** CA-re-desativar - **Regra:** #5
- **Observacoes:** -

### CT-017 - Buscar conta inexistente retorna 404 NOT_FOUND
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /accounts/{uuid-aleatorio}`
  2. Asserir status e shape de erro
- **Dados de entrada:** UUID aleatório
- **Resultado esperado:** HTTP 404 `NOT_FOUND` ("Conta não encontrada")
- **CA:** CA-404-inexistente - **Regra:** #4
- **Observacoes:** aplicar também a PUT e deactivate (mesmo `findOwned`)

### CT-018 - Buscar conta de outro usuario retorna 404 NOT_FOUND
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** dois usuários A e B; A tem conta
- **Passos:**
  1. Logar como B e enviar `GET /accounts/{id-da-conta-de-A}`
  2. Asserir 404
- **Dados de entrada:** id da conta de outro usuário
- **Resultado esperado:** HTTP 404 `NOT_FOUND` (ownership por `findByUser_IdAndId` — nunca 403)
- **CA:** CA-ownership - **Regra:** #4
- **Observacoes:** ownership vale para todos os endpoints autenticados

### CT-019 - Cadastro com campos obrigatorios ausentes retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /accounts` omitindo um campo obrigatório por vez (name, type, initialBalance)
  2. Asserir 400 e `fields`
- **Dados de entrada:** payloads parametrizados (campo ausente)
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.<campo>` preenchido (`name.required`/`type.required`/`initialBalance.required`)
- **CA:** CA-campos-ausentes - **Regra:** #8
- **Observacoes:** caso parametrizado (nome do método `deveRejeitarContaSemCampoObrigatorio`)

### CT-020 - Cadastro com saldo inicial negativo retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /accounts` com `initialBalance: -1`
  2. Asserir 400 e `fields.initialBalance`
- **Dados de entrada:** `{"name":"x","type":"CHECKING","initialBalance":-1}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`initialBalance.notNegative` = "O saldo inicial deve ser maior ou igual a 0")
- **CA:** CA-saldo-negativo-criar - **Regra:** #8
- **Observacoes:** `@DecimalMin("0.0")`

### CT-021 - Cadastro com nome maior que 100 caracteres retorna 400
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /accounts` com `name` de 101 caracteres
  2. Asserir 400 e `fields.name`
- **Dados de entrada:** name com 101 caracteres
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`name.tooLong`)
- **CA:** CA-name-too-long - **Regra:** #8
- **Observacoes:** -

### CT-022 - Cadastro com tipo invalido retorna 400 BAD_REQUEST
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /accounts` com `type: "INVESTIMENTO"`
  2. Asserir 400
- **Dados de entrada:** enum inválido no corpo
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (enum mismatch — sem `fields`)
- **CA:** CA-type-invalido - **Regra:** #8
- **Observacoes:** `HttpMessageNotReadableException`; diferencia do 400 `VALIDATION_ERROR`

### CT-023 - Atualizacao com nome/tipo ausentes retorna 400 VALIDATION_ERROR
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** conta existente
- **Passos:**
  1. Enviar `PUT /accounts/{id}` omitindo `name` (ou `type`)
  2. Asserir 400 e `fields`
- **Dados de entrada:** `AccountUpdateRequest` incompleto
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`fields.name`/`fields.type`)
- **CA:** CA-campos-ausentes - **Regra:** #8
- **Observacoes:** [SUPOSICAO] mesmo shape do POST (mesmo global handler)

### CT-024 - Saldo da conta pode ficar negativo apos despesa maior que o saldo
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** conta criada com saldo 100.00 (pré-condição via `FinanceFixture`/API)
- **Passos:**
  1. Criar transação EXPENSE de 150.00 nessa conta (via API)
  2. Buscar `GET /accounts/{id}` e asserir balance
- **Dados de entrada:** EXPENSE > saldo atual
- **Resultado esperado:** HTTP 200; `balance` = -50.00 (saldo negativo aceito)
- **CA:** CA-saldo-negativo - **Regra:** #7
- **Observacoes:** valida limitação documentada; pré-condição usa a feature transacoes

### CT-025 - Sem delete fisico de conta (endpoint nao existe)
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Tentar `DELETE /accounts/{id}`
  2. Observar resposta
- **Dados de entrada:** id de conta
- **Resultado esperado:** sem endpoint — 404/405 (fluxo não automatizável via API)
- **CA:** - **Regra:** #9
- **Observacoes:** [-]: não automatizável — não existe `@DeleteMapping` no app (inventário: sem delete físico em lugar nenhum)

### CT-026 - Desativacao e terminal (sem endpoint de reativacao)
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** API
- **Pre-condicoes:** conta desativada
- **Passos:**
  1. Procurar endpoint de reativação em `/accounts`
  2. Observar resposta para qualquer candidato (GET/POST/PUT/PATCH)
- **Dados de entrada:** id de conta INACTIVE
- **Resultado esperado:** sem endpoint de reativação — status INACTIVE é terminal
- **CA:** - **Regra:** #5
- **Observacoes:** [-]: não automatizável — não há como reativar via API; o teste de "terminal" é indireto (CT-016 re-desativar 409)

---