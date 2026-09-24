# Casos de Teste API - Contas

**Data de geração:** 2026-09-24
**Autor:** QA Agent
**Fonte:** docs/features/contas/contas.md
**Última atualização:** 2026-09-24 (agrupado por endpoint + BVA)

## Resumo Executivo
- **Total de casos de teste:** 26
- **Executados:** 0/26 (0%)
- **Distribuição por prioridade:**
  - P0: 12 (10 contratos + 2 fluxo principal)
  - P1: 8
  - P2: 4
  - P3: 2 (notas `[-]`)
- **Critérios de aceite cobertos:** 13 de 13 (API) — todos os CAs do `contas.md`
- **Regras de negócio cobertas:** 7/8 direta — regra 6 (conta inativa bloqueia movimentação) coberta pelos CTs da feature **transacoes** (409), por dedupe
- **Observações gerais:** CTs agrupados por endpoint (`POST /accounts`, `GET /accounts`, `GET /accounts/{id}`, `PUT /accounts/{id}`, `PATCH /accounts/{id}/deactivate`, saldo negativo, limitações); contratos P0 abrem a seção de cada endpoint. BVA conferido no backend (`AccountRequest`: `@Size(max=100)` no `name`, `@DecimalMin("0.0")` no `initialBalance`; `type` é enum — sem BVA de comprimento). Shape de erro compartilhado: `fields` presente **somente** em 400 `VALIDATION_ERROR` (demais erros omitem por `non_null`) — schema de erro deve ter `fields` opcional. Dedupe: 401 funcional de `/accounts` coberto pela feature **autenticacao** (CT-015); aqui o 401 do contrato é só gatilho de forma. Correção da versão anterior: validações JSR-303 não são regra de negócio (Regra `N/A` — antes, apontavam errado para #8); regra #8 = sem delete físico (CT-025).

---

## Casos de Teste

### POST /accounts

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Contrato do response de sucesso do POST /accounts | P0 | Contrato | - | - |
| [ ] | CT-002 | Contrato do response de erro do POST /accounts | P0 | Contrato | - | - |
| [ ] | CT-003 | Criar conta com dados validos retorna 201 com saldo inicial e ACTIVE | P0 | Positivo | CA-criar-conta | #1 |
| [ ] | CT-004 | Cadastro com campos obrigatorios ausentes retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-campos-ausentes | N/A |
| [ ] | CT-005 | Limites do initialBalance: 0.00/-0.01 (BVA) | P1 | Borda | CA-saldo-negativo-criar | N/A |
| [ ] | CT-006 | Limites do name: 100/101 caracteres (BVA) | P2 | Borda | CA-name-limites | N/A |
| [ ] | CT-007 | Cadastro com tipo invalido retorna 400 BAD_REQUEST | P2 | Negativo | CA-type-invalido | N/A |

### GET /accounts

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-008 | Contrato do response de sucesso do GET /accounts | P0 | Contrato | - | - |
| [ ] | CT-009 | Contrato do response de erro do GET /accounts | P0 | Contrato | - | - |
| [ ] | CT-010 | Listar contas retorna array ordenado por nome | P0 | Positivo | CA-listar | #3 |

### GET /accounts/{id}

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-011 | Contrato do response de sucesso do GET /accounts/{id} | P0 | Contrato | - | - |
| [ ] | CT-012 | Contrato do response de erro do GET /accounts/{id} | P0 | Contrato | - | - |
| [ ] | CT-013 | Buscar conta por id retorna a conta | P1 | Positivo | CA-buscar | #4 |
| [ ] | CT-014 | Buscar conta inexistente retorna 404 NOT_FOUND | P1 | Negativo | CA-404-inexistente | #4 |
| [ ] | CT-015 | Buscar conta de outro usuario retorna 404 NOT_FOUND | P1 | Negativo | CA-ownership | #4 |

### PUT /accounts/{id}

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-016 | Contrato do response de sucesso do PUT /accounts/{id} | P0 | Contrato | - | - |
| [ ] | CT-017 | Contrato do response de erro do PUT /accounts/{id} | P0 | Contrato | - | - |
| [ ] | CT-018 | Atualizar conta altera nome/tipo e preserva initialBalance e balance | P1 | Positivo | CA-atualizar | #2 |
| [ ] | CT-019 | Atualizacao com nome/tipo ausentes retorna 400 VALIDATION_ERROR | P2 | Negativo | CA-campos-ausentes | N/A |

### PATCH /accounts/{id}/deactivate

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-020 | Contrato do response de sucesso do PATCH /accounts/{id}/deactivate | P0 | Contrato | - | - |
| [ ] | CT-021 | Contrato do response de erro do PATCH /accounts/{id}/deactivate | P0 | Contrato | - | - |
| [ ] | CT-022 | Desativar conta retorna 204 e o status muda para INACTIVE | P1 | Positivo | CA-desativar | #5 |
| [ ] | CT-023 | Desativar conta ja inativa retorna 409 | P1 | Negativo | CA-re-desativar | #5 |

### Saldo pode negativar (regra #7) — alvo: GET /accounts/{id}

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-024 | Saldo da conta pode ficar negativo apos despesa maior que o saldo | P2 | Positivo | CA-saldo-negativo | #7 |

### Limitações da feature (não automatizáveis)

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-025 | Sem delete fisico de conta (endpoint nao existe) | P3 | Normal | - | #8 |
| [ ] | CT-026 | Desativacao e terminal (sem endpoint de reativacao) | P3 | Normal | - | #5 |

---

## Detalhamento por endpoint

### POST /accounts

### CT-001 - Contrato do response de sucesso do POST /accounts
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/contas/account-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado via API (`TestUserFixture`)
- **Passos:**
  1. Enviar `POST /accounts` com payload válido e único
  2. Validar o corpo (201) contra o schema estrito
- **Dados de entrada:** `AccountRequest` faker (name único, type, initialBalance ≥ 0)
- **Resultado esperado:** HTTP 201; body `{id,name,type,initialBalance,balance,status,createdAt,updatedAt}` conforme schema (`additionalProperties: false`); qualquer campo ausente/extra/tipo errado falha
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas (`schemas/contas/*` ainda não existe)

### CT-002 - Contrato do response de erro do POST /accounts
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /accounts` com body `{}` (validação dos 3 campos)
  2. Validar o corpo do erro (400) contra o schema
- **Dados de entrada:** `{}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR`; body `{timestamp,status,error,message,path,fields}` conforme schema — `fields` mapeia `name`/`type`/`initialBalance`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas; `fields` é opcional no schema (só presente em 400 `VALIDATION_ERROR` — demais erros omitem). Forma alternativa de erro: `type` inválido → 400 `BAD_REQUEST` sem `fields` (coberto funcionalmente em CT-007)

### CT-003 - Criar conta com dados validos retorna 201 com saldo inicial e ACTIVE
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário registrado via API (`TestUserFixture`)
- **Passos:**
  1. Enviar `POST /accounts` com `name`/`type`/`initialBalance` válidos
  2. Asserir 201 e campos do `AccountResponse`
- **Dados de entrada:** conta CHECKING com saldo 1000.00 e nome único (faker)
- **Resultado esperado:** HTTP 201; `id` retornado; `balance` = `initialBalance`; `status` `ACTIVE`
- **CA:** CA-criar-conta - **Regra:** #1
- **Observacoes:** regra 1 (`balance` nasce igual a `initialBalance`, `AccountService.create`); massa única via `UserFaker`

### CT-004 - Cadastro com campos obrigatorios ausentes retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /accounts` omitindo um campo obrigatório por vez (name, type, initialBalance)
  2. Asserir 400 e `fields` apontando o campo
- **Dados de entrada:** payloads parametrizados (campo ausente; versão base `{}`)
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.<campo>` preenchido (`name.required`/`type.required`/`initialBalance.required`)
- **CA:** CA-campos-ausentes - **Regra:** N/A
- **Observacoes:** JSR-303 (`AccountRequest`); caso parametrizado — método único `deveRejeitarContaSemCampoObrigatorio`

### CT-005 - Limites do initialBalance: 0.00/-0.01 (BVA)
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /accounts` com `initialBalance` = `0.00` → 201
  2. Repetir com `initialBalance` = `-0.01` → 400
- **Dados de entrada:** `initialBalance` 0.00 e -0.01 (BVA: min=0.00, min-1=-0.01)
- **Resultado esperado:** `0.00` → 201 com `balance` = 0.00; `-0.01` → 400 `VALIDATION_ERROR` (`fields.initialBalance` = `initialBalance.notNegative`)
- **CA:** CA-saldo-negativo-criar - **Regra:** N/A
- **Observacoes:** limite real `@DecimalMin("0.0")` conferido no backend (`AccountRequest.java:24`). `BVA: min-1=-0.01, min=0.00`. Borda aceita usa saldo único por execução. Antes testava apenas -1 (substituído pelo limite exato)

### CT-006 - Limites do name: 100/101 caracteres (BVA)
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /accounts` com `name` de 100 caracteres → 201
  2. Repetir com `name` de 101 caracteres → 400
- **Dados de entrada:** nomes de 100 e 101 caracteres (BVA: max=100, max+1=101)
- **Resultado esperado:** `100` → 201; `101` → 400 `VALIDATION_ERROR` (`fields.name` = `name.tooLong`)
- **CA:** CA-name-limites - **Regra:** N/A
- **Observacoes:** limite real `@Size(max=100)` conferido no backend (`AccountRequest.java:15`). `BVA: max=100, max+1=101`. Só tem `max` (mínimo é `@NotBlank` — vazio coberto por CT-004); borda aceita com valor único por execução (sufixo faker). Antes testava apenas 101

### CT-007 - Cadastro com tipo invalido retorna 400 BAD_REQUEST
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /accounts` com `type: "INVESTIMENTO"`
  2. Asserir 400
- **Dados de entrada:** enum inválido no corpo
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (enum mismatch — sem `fields`)
- **CA:** CA-type-invalido - **Regra:** N/A
- **Observacoes:** `HttpMessageNotReadableException`; diferencia do 400 `VALIDATION_ERROR` (CT-004) — sem `fields`

### GET /accounts

### CT-008 - Contrato do response de sucesso do GET /accounts
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/contas/account-list-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com ao menos 1 conta
- **Passos:**
  1. Enviar `GET /accounts`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** sem query params
- **Resultado esperado:** HTTP 200; body é um **array** de `AccountResponse` (sem paginação)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas; sem paginação (`findByUser_IdOrderByName`)

### CT-009 - Contrato do response de erro do GET /accounts
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /accounts` sem `Authorization`
  2. Validar o corpo do erro (401) contra o schema
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401; body `{timestamp,status,error,message,path}` conforme schema (sem `fields`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas; o 401 é **gatilho de forma**, não testa autorização — a proteção 401 funcional de `/accounts` é coberta pela feature **autenticacao** (CT-015)

### CT-010 - Listar contas retorna array ordenado por nome
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com 3 contas criadas (nomes fora de ordem no POST)
- **Passos:**
  1. Enviar `GET /accounts`
  2. Asserir 200 e ordem dos `name`
- **Dados de entrada:** 3 contas criadas em sequência com nomes fora de ordem (prefixo único do faker + sufixos A/B/C)
- **Resultado esperado:** HTTP 200; array com `name` em ordem alfabética — sem paginação; somente as contas do usuário logado
- **CA:** CA-listar - **Regra:** #3
- **Observacoes:** regra 3 (`findByUser_IdOrderByName`); massa única por execução para não colidir com contas pré-existentes do usuário

### GET /accounts/{id}

### CT-011 - Contrato do response de sucesso do GET /accounts/{id}
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
- **CA:** - **Regra:** - **Observacoes:** shape idêntico ao do POST (mesmo DTO `AccountResponse`); mantido por endpoint conforme convenção de contrato — nunca combinado

### CT-012 - Contrato do response de erro do GET /accounts/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /accounts/{uuid-aleatorio}` (inexistente)
  2. Validar o corpo do erro (404) contra o schema
- **Dados de entrada:** UUID aleatório
- **Resultado esperado:** HTTP 404 `NOT_FOUND`; body `{timestamp,status,error,message,path}` conforme schema (`message` = "Conta não encontrada", sem `fields`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-013 - Buscar conta por id retorna a conta
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com 1 conta criada
- **Passos:**
  1. Enviar `GET /accounts/{id}`
  2. Asserir 200 e campos
- **Dados de entrada:** id da conta criada
- **Resultado esperado:** HTTP 200; `id`/`name`/`type`/`balance`/`status` conforme a criação
- **CA:** CA-buscar - **Regra:** #4
- **Observacoes:** -

### CT-014 - Buscar conta inexistente retorna 404 NOT_FOUND
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
- **Observacoes:** mesmo `findOwned` de PUT/deactivate — aplica-se a todos os endpoints com id

### CT-015 - Buscar conta de outro usuario retorna 404 NOT_FOUND
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** dois usuários A e B (fixtures); A tem conta
- **Passos:**
  1. Autenticar como B e enviar `GET /accounts/{id-da-conta-de-A}`
  2. Asserir 404
- **Dados de entrada:** id da conta de outro usuário
- **Resultado esperado:** HTTP 404 `NOT_FOUND` (ownership por `findByUser_IdAndId` — nunca 403)
- **CA:** CA-ownership - **Regra:** #4
- **Observacoes:** ownership vale para todos os endpoints autenticados (GET/PUT/deactivate)

### PUT /accounts/{id}

### CT-016 - Contrato do response de sucesso do PUT /accounts/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/contas/account-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com 1 conta
- **Passos:**
  1. Enviar `PUT /accounts/{id}` com novo `name`/`type`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** `AccountUpdateRequest` válido (sem `initialBalance` no DTO)
- **Resultado esperado:** HTTP 200; body conforme schema com `initialBalance`/`balance` preservados
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas; `AccountUpdateRequest` só tem `name`/`type`

### CT-017 - Contrato do response de erro do PUT /accounts/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `PUT /accounts/{uuid-aleatorio}` com body **válido** (`name`/`type` ok)
  2. Validar o corpo do erro (404) contra o schema
- **Dados de entrada:** UUID aleatório + `AccountUpdateRequest` válido
- **Resultado esperado:** HTTP 404 `NOT_FOUND`; body `{timestamp,status,error,message,path}` conforme schema (sem `fields`)
- **CA:** - **Regra:** - **Observacoes:** body válido garante que o erro vem do `findOwned` (404), não da validação; validação funcional fica em CT-019. Depende da infraestrutura de schemas

### CT-018 - Atualizar conta altera nome/tipo e preserva initialBalance e balance
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com conta criada com saldo 1000.00
- **Passos:**
  1. Enviar `PUT /accounts/{id}` com novo `name` e `type` SAVINGS
  2. Asserir 200 e buscar a conta de novo (`GET /accounts/{id}`)
- **Dados de entrada:** `{"name":"<novo único>","type":"SAVINGS"}` (sem `initialBalance`)
- **Resultado esperado:** HTTP 200; `name`/`type` atualizados mas `initialBalance` e `balance` intactos (1000.00)
- **CA:** CA-atualizar - **Regra:** #2
- **Observacoes:** regra 2 — `PUT` não aceita/ignora `initialBalance` (`AccountUpdateRequest` não tem o campo); sem endpoint de ajuste manual de saldo

### CT-019 - Atualizacao com nome/tipo ausentes retorna 400 VALIDATION_ERROR
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** conta existente do usuário
- **Passos:**
  1. Enviar `PUT /accounts/{id}` omitindo `name` (e depois `type`)
  2. Asserir 400 e `fields`
- **Dados de entrada:** `AccountUpdateRequest` incompleto (campo ausente por vez)
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.name`/`fields.type` (`name.required`/`type.required`)
- **CA:** CA-campos-ausentes - **Regra:** N/A
- **Observacoes:** [SUPOSICAO] mesmo shape do POST (mesmo global handler) — validar empiricamente ao implementar; a validação roda antes do `findOwned`, então id existente não é pré-condição

### PATCH /accounts/{id}/deactivate

### CT-020 - Contrato do response de sucesso do PATCH /accounts/{id}/deactivate
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** n/a (sem corpo — validação de status 204)
- **Pre-condicoes:** usuário autenticado com 1 conta ativa
- **Passos:**
  1. Enviar `PATCH /accounts/{id}/deactivate`
  2. Validar HTTP 204 e corpo vazio
- **Dados de entrada:** id de conta ativa
- **Resultado esperado:** HTTP 204 No Content; body vazio (sem headers de corpo)
- **CA:** - **Regra:** - **Observacoes:** sem schema aplicável (response sem corpo); validar status e corpo vazio

### CT-021 - Contrato do response de erro do PATCH /accounts/{id}/deactivate
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** uma conta já desativada
- **Passos:**
  1. Reenviar `PATCH /accounts/{id}/deactivate` na conta inativa
  2. Validar o corpo do erro (409) contra o schema
- **Dados de entrada:** id de conta INACTIVE
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION`; body `{timestamp,status,error,message,path}` conforme schema (`message` = "A conta já está inativa", sem `fields`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas; a assert de regra/mensagem fica no CT-023 (contrato nunca substitui o funcional)

### CT-022 - Desativar conta retorna 204 e o status muda para INACTIVE
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com conta ativa
- **Passos:**
  1. Enviar `PATCH /accounts/{id}/deactivate`
  2. Asserir 204 sem corpo e buscar a conta (`GET /accounts/{id}`)
- **Dados de entrada:** id de conta ACTIVE
- **Resultado esperado:** HTTP 204 sem corpo; `GET /accounts/{id}` retorna `status` `INACTIVE`
- **CA:** CA-desativar - **Regra:** #5
- **Observacoes:** regra 5 — desativação lógica (sem delete físico)

### CT-023 - Desativar conta ja inativa retorna 409
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
- **Observacoes:** indiretamente também prova a terminalidade (CT-026); não existe reativação

### Saldo pode negativar (regra #7) — alvo: GET /accounts/{id}

### CT-024 - Saldo da conta pode ficar negativo apos despesa maior que o saldo
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** conta criada com saldo 100.00 (pré-condição via API/`AccountClient`); cliente/fixture de transações disponível
- **Passos:**
  1. Criar transação EXPENSE de 150.00 nessa conta (via API — `POST /transactions`, apoio)
  2. Buscar `GET /accounts/{id}` e asserir o `balance`
- **Dados de entrada:** EXPENSE 150.00 > saldo 100.00 (categoria EXPENSE necessária)
- **Resultado esperado:** HTTP 200; `balance` = -50.00 (saldo negativo aceito — sem CHECK no DB, sem cobertura)
- **CA:** CA-saldo-negativo - **Regra:** #7
- **Observacoes:** regra 7; `POST /transactions` é **apoio** (não cria seção própria); pré-condição usa a feature **transacoes**

### Limitações da feature (não automatizáveis)

### CT-025 - Sem delete fisico de conta (endpoint nao existe)
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Tentar `DELETE /accounts/{id}`
  2. Observar resposta
- **Dados de entrada:** id de conta
- **Resultado esperado:** sem endpoint — 404/405 (fluxo não automatizável como caso funcional)
- **CA:** - **Regra:** #8
- **Observacoes:** [-]: não automatizável — não existe `@DeleteMapping` no app (inventário: sem delete físico em lugar nenhum); mantido como nota de limitação

### CT-026 - Desativacao e terminal (sem endpoint de reativacao)
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** API
- **Pre-condicoes:** conta desativada
- **Passos:**
  1. Procurar endpoint de reativação em `/accounts` (GET/POST/PUT/PATCH candidatos)
  2. Observar resposta
- **Dados de entrada:** id de conta INACTIVE
- **Resultado esperado:** sem endpoint de reativação — status INACTIVE é terminal
- **CA:** - **Regra:** #5
- **Observacoes:** [-]: não automatizável — não há como reativar via API; o teste de "terminal" é indireto (CT-023 re-desativar → 409)

---
