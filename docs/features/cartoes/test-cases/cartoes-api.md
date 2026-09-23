# Casos de Teste API - Cartões de Crédito

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/cartoes/cartoes.md

## Resumo Executivo
- **Total de casos de teste:** 32
- **Executados:** 0/32 (0%)
- **Distribuição por prioridade:**
  - P0: 15
  - P1: 8
  - P2: 7
  - P3: 2
- **Critérios de aceite cobertos:** 11 de 11 (API) após revisão — incluído CA-id-nao-uuid (CT-032)
- **Regras de negócio cobertas:** 13/13 — regra 1 corrigida no CT-016 (usedLimit = soma de parcelas OPEN)
- **Observações gerais:** `usedLimit`/`availableLimit` são calculados dinamicamente. Revisão:
  CT-032 (duplicado do CT-031) substituído por gap de `id` não-UUID → 400; CT-031 marcado `[-]`.
  Feature nova, nenhum teste automatizado.
  (não confundir com `creditLimit` cadastrado). Feature nova, nenhum teste automatizado.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Contrato do response de sucesso do POST /credit-cards | P0 | Contrato | - | - |
| [ ] | CT-002 | Contrato do response de erro do POST /credit-cards | P0 | Contrato | - | - |
| [ ] | CT-003 | Contrato do response de sucesso do GET /credit-cards | P0 | Contrato | - | - |
| [ ] | CT-004 | Contrato do response de erro do GET /credit-cards | P0 | Contrato | - | - |
| [ ] | CT-005 | Contrato do response de sucesso do GET /credit-cards/{id} | P0 | Contrato | - | - |
| [ ] | CT-006 | Contrato do response de erro do GET /credit-cards/{id} | P0 | Contrato | - | - |
| [ ] | CT-007 | Contrato do response de sucesso do PUT /credit-cards/{id} | P0 | Contrato | - | - |
| [ ] | CT-008 | Contrato do response de erro do PUT /credit-cards/{id} | P0 | Contrato | - | - |
| [ ] | CT-009 | Contrato do response do PATCH /credit-cards/{id}/deactivate (204) | P0 | Contrato | - | - |
| [ ] | CT-010 | Contrato do response de erro do PATCH /credit-cards/{id}/deactivate | P0 | Contrato | - | - |
| [ ] | CT-011 | Criar cartão retorna 201 com usedLimit 0 e availableLimit igual ao limite | P0 | Positivo | CA-criar-feliz | #1 |
| [ ] | CT-012 | Listar retorna array ordenado por nome com limites calculados | P0 | Positivo | CA-listar-feliz | #9 |
| [ ] | CT-013 | Buscar cartão por id retorna 200 | P0 | Positivo | CA-get-feliz | #9 |
| [ ] | CT-014 | Atualizar cartão retorna 200 com novos valores | P0 | Positivo | CA-put-feliz | - |
| [ ] | CT-015 | Desativar cartão ativo retorna 204 e status INACTIVE | P0 | Positivo | CA-deactivate-feliz | #5 |
| [ ] | CT-016 | usedLimit/availableLimit dinâmicos refletem soma das parcelas OPEN | P1 | Positivo | CA-limits-dinamicos | #1 |
| [ ] | CT-017 | Re-desativar cartão inativo retorna 409 | P1 | Negativo | CA-already-inactive | #4 |
| [ ] | CT-018 | Compra em cartão inativo retorna 409 | P1 | Negativo | CA-card-inativo-compra | #3 |
| [ ] | CT-019 | GET de id inexistente retorna 404 | P1 | Negativo | CA-get-404 | #7 |
| [ ] | CT-020 | GET de id de outro usuário retorna 404 | P1 | Negativo | CA-ownership-get | #7 |
| [ ] | CT-021 | PUT em id inexistente ou de outro usuário retorna 404 | P1 | Negativo | CA-ownership-put | #7 |
| [ ] | CT-022 | PATCH em id inexistente ou de outro usuário retorna 404 | P1 | Negativo | CA-ownership-patch | #7 |
| [ ] | CT-023 | Alterar closingDay/dueDay não altera faturas existentes | P1 | Borda | CA-days-imutaveis | #6 |
| [ ] | CT-024 | Campos obrigatórios ausentes no POST retornam 400 VALIDATION_ERROR | P2 | Negativo | CA-campos-ausentes | #8 |
| [ ] | CT-025 | Name em branco ou acima de 100 retorna 400 | P2 | Negativo | CA-name-invalido | #8 |
| [ ] | CT-026 | CreditLimit menor que 0.01 retorna 400 | P2 | Negativo | CA-limit-invalido | #8 |
| [ ] | CT-027 | closingDay/dueDay fora de 1 a 31 retornam 400 | P2 | Negativo | CA-day-invalido | #8 |
| [ ] | CT-028 | PUT com campos inválidos retorna 400 | P2 | Negativo | CA-put-invalido | #8 |
| [ ] | CT-029 | JSON malformado retorna 400 BAD_REQUEST | P2 | Negativo | CA-json-malformado | #8 |
| [ ] | CT-030 | Endpoint autenticado sem token retorna 401 | P2 | Negativo | CA-token-ausente | - |
| [-] | CT-031 | [-] Desativação terminal: sem endpoint de reativação | P3 | Borda | CA-sem-reativacao | #5 |
| [ ] | CT-032 | PATCH /credit-cards/{id}/deactivate com id fora do formato UUID retorna 400 BAD_REQUEST | P3 | Negativo | CA-id-nao-uuid | #13 |

---

### CT-001 - Contrato do response de sucesso do POST /credit-cards
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/cartoes/credit-card-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado
- **Passos:**
  1. Enviar `POST /credit-cards` com payload válido
  2. Validar o corpo (201) contra o schema estrito
- **Dados de entrada:** `{name, creditLimit, closingDay, dueDay}`
- **Resultado esperado:** HTTP 201; body `{id, name, creditLimit, usedLimit, availableLimit, closingDay, dueDay, status, createdAt, updatedAt}`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas; método sugerido: `deveValidarContratoDeSucessoAoCriarCartao`

### CT-002 - Contrato do response de erro do POST /credit-cards
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /credit-cards` com payload inválido (ex.: creditLimit 0)
  2. Validar o corpo de erro (400) contra o schema
- **Dados de entrada:** `{"name": "X", "creditLimit": 0}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` + `fields` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-003 - Contrato do response de sucesso do GET /credit-cards
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/cartoes/list-response.json` (a criar)
- **Pre-condicoes:** 1+ cartão criado
- **Passos:**
  1. Enviar `GET /credit-cards`
  2. Validar o corpo (200) contra o schema de `array`
- **Dados de entrada:** -
- **Resultado esperado:** HTTP 200; `CreditCardResponse[]` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-004 - Contrato do response de erro do GET /credit-cards
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /credit-cards` **sem** token
  2. Validar o corpo (401) contra o schema
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401 `UNAUTHORIZED`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-005 - Contrato do response de sucesso do GET /credit-cards/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/cartoes/credit-card-response.json` (a criar)
- **Pre-condicoes:** 1 cartão criado
- **Passos:**
  1. Enviar `GET /credit-cards/{id}`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** `id` do cartão
- **Resultado esperado:** HTTP 200; mesmo shape do CT-001
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-006 - Contrato do response de erro do GET /credit-cards/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /credit-cards/{id}` com id inexistente
  2. Validar o corpo (404) contra o schema
- **Dados de entrada:** UUID v4 aleatório
- **Resultado esperado:** HTTP 404 `NOT_FOUND` (`creditCard.notFound`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-007 - Contrato do response de sucesso do PUT /credit-cards/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/cartoes/credit-card-response.json` (a criar)
- **Pre-condicoes:** 1 cartão criado
- **Passos:**
  1. Enviar `PUT /credit-cards/{id}` com payload válido
  2. Validar o corpo (200) contra o schema
- **Dados de entrada:** payload atualizado
- **Resultado esperado:** HTTP 200; body conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-008 - Contrato do response de erro do PUT /credit-cards/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** 1 cartão criado
- **Passos:**
  1. Enviar `PUT /credit-cards/{id}` com payload inválido
  2. Validar o corpo (400) contra o schema
- **Dados de entrada:** `{"name": ""}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` + `fields`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-009 - Contrato do response do PATCH /credit-cards/{id}/deactivate (204)
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `-` (204 não tem body)
- **Pre-condicoes:** 1 cartão ACTIVE
- **Passos:**
  1. Enviar `PATCH /credit-cards/{id}/deactivate`
  2. Validar status 204 e corpo **vazio**
- **Dados de entrada:** `id` do cartão
- **Resultado esperado:** HTTP 204 No Content; sem body
- **CA:** - **Regra:** - **Observacoes:** 204 sem body — schema não se aplica; valida status + corpo vazio

### CT-010 - Contrato do response de erro do PATCH /credit-cards/{id}/deactivate
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** 1 cartão ACTIVE com status INACTIVE (desativado antes)
- **Passos:**
  1. Enviar `PATCH /credit-cards/{id}/deactivate` no cartão já inativo
  2. Validar o corpo (409) contra o schema
- **Dados de entrada:** cartão INACTIVE
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` (`creditCard.alreadyInactive`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-011 - Criar cartão retorna 201 com usedLimit 0 e availableLimit igual ao limite
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário autenticado
- **Passos:**
  1. Enviar `POST /credit-cards`
  2. Asserir 201, `status=ACTIVE`, `usedLimit=0`, `availableLimit=creditLimit`
- **Dados de entrada:** payload faker (name, limit, closing/due day 1-31)
- **Resultado esperado:** HTTP 201; limites conforme o cadastro
- **CA:** CA-criar-feliz - **Regra:** #1
- **Observacoes:** método sugerido: `deveCriarCartaoComLimitesZerados`

### CT-012 - Listar retorna array ordenado por nome com limites calculados
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** 2+ cartões
- **Passos:**
  1. Enviar `GET /credit-cards`
  2. Asserir 200, ordem por nome e presença de `usedLimit`/`availableLimit`
- **Dados de entrada:** -
- **Resultado esperado:** HTTP 200; array `List` (sem paginação), ordenado por nome
- **CA:** CA-listar-feliz - **Regra:** #9
- **Observacoes:** método sugerido: `deveListarCartoesOrdenadosPorNome`

### CT-013 - Buscar cartão por id retorna 200
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** 1 cartão criado
- **Passos:**
  1. Enviar `GET /credit-cards/{id}`
  2. Asserir 200 e campos
- **Dados de entrada:** `id` do create
- **Resultado esperado:** HTTP 200; `usedLimit` recalculado no momento da leitura
- **CA:** CA-get-feliz - **Regra:** #9
- **Observacoes:** método sugerido: `deveBuscarCartaoPorId`

### CT-014 - Atualizar cartão retorna 200 com novos valores
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** 1 cartão criado
- **Passos:**
  1. Enviar `PUT /credit-cards/{id}` com novo name/limit/days
  2. Asserir 200 e refletimento
- **Dados de entrada:** payload atualizado (faker)
- **Resultado esperado:** HTTP 200; campos atualizados; `usedLimit` preservado
- **CA:** CA-put-feliz - **Regra:** -
- **Observacoes:** -

### CT-015 - Desativar cartão ativo retorna 204 e status INACTIVE
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** 1 cartão ACTIVE
- **Passos:**
  1. Enviar `PATCH /credit-cards/{id}/deactivate`
  2. Asserir 204
  3. Confirmar `status=INACTIVE` via `GET /credit-cards/{id}`
- **Dados de entrada:** `id` do cartão
- **Resultado esperado:** HTTP 204; `status` muda para `INACTIVE`
- **CA:** CA-deactivate-feliz - **Regra:** #5
- **Observacoes:** método sugerido: `deveDesativarCartao`

### CT-016 - usedLimit/availableLimit dinâmicos refletem soma das parcelas OPEN
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** cartão + compra registrada (parcelas OPEN)
- **Passos:**
  1. Criar cartão com limite conhecido
  2. Registrar compra via API
  3. Consultar `GET /credit-cards/{id}`
- **Dados de entrada:** compra = 100 em 2x, creditLimit = 1000
- **Resultado esperado:** `usedLimit == 100` (soma das parcelas OPEN); `availableLimit == 900` (`creditLimit - usedLimit`)
- **CA:** CA-limits-dinamicos - **Regra:** #1
- **Observacoes:** corrigido na revisão: usedLimit **não** é o creditLimit cadastrado — é calculado (`Sum(parcelas OPEN)`); availableLimit = creditLimit − usedLimit (`CreditCardServiceImpl`)

### CT-017 - Re-desativar cartão inativo retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** cartão INACTIVE (desativado antes)
- **Passos:**
  1. Enviar `PATCH /credit-cards/{id}/deactivate`
  2. Asserir 409
- **Dados de entrada:** cartão já inativo
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` (`creditCard.alreadyInactive`)
- **CA:** CA-already-inactive - **Regra:** #4
- **Observacoes:** -

### CT-018 - Compra em cartão inativo retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** cartão INACTIVE + categoria EXPENSE ACTIVE (pré-condição outro recurso)
- **Passos:**
  1. Enviar `POST /purchases` apontando `creditCardId` para o cartão inativo
  2. Asserir 409
- **Dados de entrada:** purchase válida mas cartão inativo
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` (`creditCard.notActive`)
- **CA:** CA-card-inativo-compra - **Regra:** #3
- **Observacoes:** pré-condição de outro recurso (compras)

### CT-019 - GET de id inexistente retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /credit-cards/{id}` com id aleatório
  2. Asserir 404
- **Dados de entrada:** UUID v4 aleatório
- **Resultado esperado:** HTTP 404 (`creditCard.notFound`)
- **CA:** CA-get-404 - **Regra:** #7
- **Observacoes:** -

### CT-020 - GET de id de outro usuário retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário de apoio com cartão
- **Passos:**
  1. Enviar `GET /credit-cards/{id}` com id do cartão do outro usuário
  2. Asserir 404
- **Dados de entrada:** id do outro usuário
- **Resultado esperado:** HTTP 404 (ownership via `findByUser_IdAndId`; nunca 403)
- **CA:** CA-ownership-get - **Regra:** #7
- **Observacoes:** -

### CT-021 - PUT em id inexistente ou de outro usuário retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário de apoio com cartão (ou nenhum)
- **Passos:**
  1. Enviar `PUT /credit-cards/{id}` com id inexistente ou de outro usuário
  2. Asserir 404
- **Dados de entrada:** parametrizado
- **Resultado esperado:** HTTP 404 (`creditCard.notFound`)
- **CA:** CA-ownership-put - **Regra:** #7
- **Observacoes:** parametrizado (inexistente + outro usuário)

### CT-022 - PATCH em id inexistente ou de outro usuário retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** idem CT-021
- **Passos:**
  1. Enviar `PATCH /credit-cards/{id}/deactivate` com id inválido
  2. Asserir 404
- **Dados de entrada:** parametrizado
- **Resultado esperado:** HTTP 404 (`creditCard.notFound`)
- **CA:** CA-ownership-patch - **Regra:** #7
- **Observacoes:** parametrizado

### CT-023 - Alterar closingDay/dueDay não altera faturas existentes
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** cartão + compra → fatura criada (closing/due derivados)
- **Passos:**
  1. Registrar compra no cartão (gera fatura OPEN)
  2. Enviar `PUT /credit-cards/{id}` alterando `closingDay`/`dueDay`
  3. Consultar `GET /invoices/{id}` da fatura
- **Dados de entrada:** mudança de closing 10→20, due 15→25
- **Resultado esperado:** 200; fatura mantém `closingDate`/`dueDate` originais
- **CA:** CA-days-imutaveis - **Regra:** #6
- **Observacoes:** faturas guardam datas próprias (`entity/Invoice`)

### CT-024 - Campos obrigatórios ausentes no POST retornam 400 VALIDATION_ERROR
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /credit-cards` omitindo um campo por vez (name, creditLimit, closingDay, dueDay)
  2. Asserir 400 e `fields`
- **Dados de entrada:** payloads parametrizados
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`*.required`)
- **CA:** CA-campos-ausentes - **Regra:** #8
- **Observacoes:** parametrizado

### CT-025 - Name em branco ou acima de 100 retorna 400
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /credit-cards` com `name` vazio e depois com 101 chars
  2. Asserir 400
- **Dados de entrada:** `name: ""` e `name: "<101 chars>"`
- **Resultado esperado:** HTTP 400 (`name.required` / `name.tooLong`)
- **CA:** CA-name-invalido - **Regra:** #8
- **Observacoes:** -

### CT-026 - CreditLimit menor que 0.01 retorna 400
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /credit-cards` com `creditLimit: 0`
  2. Asserir 400
- **Dados de entrada:** `creditLimit: 0`
- **Resultado esperado:** HTTP 400 (`creditLimit.greaterThanZero`)
- **CA:** CA-limit-invalido - **Regra:** #8
- **Observacoes:** -

### CT-027 - closingDay/dueDay fora de 1 a 31 retornam 400
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /credit-cards` com `closingDay: 0` e `dueDay: 32`
  2. Asserir 400
- **Dados de entrada:** dias fora da faixa
- **Resultado esperado:** HTTP 400 (`day.between1and31`)
- **CA:** CA-day-invalido - **Regra:** #8
- **Observacoes:** -

### CT-028 - PUT com campos inválidos retorna 400
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** 1 cartão criado
- **Passos:**
  1. Enviar `PUT /credit-cards/{id}` com payload inválido
  2. Asserir 400
- **Dados de entrada:** `creditLimit: 0`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR`
- **CA:** CA-put-invalido - **Regra:** #8
- **Observacoes:** -

### CT-029 - JSON malformado retorna 400 BAD_REQUEST
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /credit-cards` com corpo inválido (JSON truncado)
  2. Asserir 400
- **Dados de entrada:** `{` (inválido)
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (`badRequest.malformed`)
- **CA:** CA-json-malformado - **Regra:** #8
- **Observacoes:** -

### CT-030 - Endpoint autenticado sem token retorna 401
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /credit-cards` sem `Authorization`
  2. Asserir 401
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401 `UNAUTHORIZED`
- **CA:** CA-token-ausente - **Regra:** -
- **Observacoes:** -

### CT-031 - [-] Desativação terminal: sem endpoint de reativação
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Verificar controlador: só `POST/GET/PUT/PATCH.../deactivate`, sem reativação
- **Dados de entrada:** -
- **Resultado esperado:** `INACTIVE` é estado terminal
- **CA:** CA-sem-reativacao - **Regra:** #5
- **Observacoes:** [ - ] limitação do app — verificável por inspeção do controlador

### CT-032 - PATCH /credit-cards/{id}/deactivate com id fora do formato UUID retorna 400 BAD_REQUEST
- **Prioridade:** P3
- **Tipo:** Negativo
- **Camada:** API
- **Schema:** `-`
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `PATCH /credit-cards/nao-e-uuid/deactivate`
  2. Asserir 400
- **Dados de entrada:** `id` não-UUID na URL
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (`MethodArgumentTypeMismatchException` → `badRequest.malformed`)
- **CA:** CA-id-nao-uuid - **Regra:** #13
- **Observacoes:** adicionado na revisão (gap de cobertura; substitui o duplicado do CT-031); método sugerido: `deveRetornarBadRequestParaIdForaDoFormatoUuid`

---