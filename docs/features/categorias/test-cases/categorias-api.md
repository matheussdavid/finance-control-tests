# Casos de Teste API - Categorias

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/categorias/categorias.md

## Resumo Executivo
- **Total de casos de teste:** 27
- **Executados:** 0/27 (0%)
- **Distribuição por prioridade:**
  - P0: 12 (10 contratos + 2 fluxo principal)
  - P1: 9
  - P2: 4
  - P3: 2 (notas `[-]`)
- **Critérios de aceite cobertos:** 12 de 12 (API)
- **Regras de negócio cobertas:** 6/8 direta — regras 6 e 7 (uso de categoria
  inativa/tipo incompatível em transação) cobertas pelos CTs da feature
  **transacoes** (409), por dedupe
- **Observações gerais:** 9 dos 10 contratos dependem de schema ainda não criado
  (`schemas/categorias/*`, `schemas/common/error-response.json`). Revisão:
  combinado `?type=INCOME`/`?type=EXPENSE` em um CT parametrizado e removido
  CT de `name>100` no PUT (duplica validação do POST).

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Contrato do response de sucesso do POST /categories | P0 | Contrato | - | - |
| [ ] | CT-002 | Contrato do response de erro do POST /categories | P0 | Contrato | - | - |
| [ ] | CT-003 | Contrato do response de sucesso do GET /categories | P0 | Contrato | - | - |
| [ ] | CT-004 | Contrato do response de erro do GET /categories | P0 | Contrato | - | - |
| [ ] | CT-005 | Contrato do response de sucesso do GET /categories/{id} | P0 | Contrato | - | - |
| [ ] | CT-006 | Contrato do response de erro do GET /categories/{id} | P0 | Contrato | - | - |
| [ ] | CT-007 | Contrato do response de sucesso do PUT /categories/{id} | P0 | Contrato | - | - |
| [ ] | CT-008 | Contrato do response de erro do PUT /categories/{id} | P0 | Contrato | - | - |
| [ ] | CT-009 | Contrato do response de sucesso do PATCH /categories/{id}/deactivate | P0 | Contrato | - | - |
| [ ] | CT-010 | Contrato do response de erro do PATCH /categories/{id}/deactivate | P0 | Contrato | - | - |
| [ ] | CT-011 | Criar categoria com dados validos retorna 201 com status ACTIVE | P0 | Positivo | CA-criar | #1 |
| [ ] | CT-012 | Listar categorias retorna array ordenado por nome | P0 | Positivo | CA-listar | #2 |
| [ ] | CT-013 | Listar categorias filtrando por tipo (?type=INCOME|EXPENSE) | P1 | Positivo | CA-listar-tipo | #2 |
| [ ] | CT-014 | Buscar categoria por id retorna a categoria | P1 | Positivo | CA-buscar | #3 |
| [ ] | CT-015 | Atualizar categoria altera nome/tipo | P1 | Positivo | CA-atualizar | #3 |
| [ ] | CT-016 | Desativar categoria retorna 204 e status INACTIVE | P1 | Positivo | CA-desativar | #5 |
| [ ] | CT-017 | Desativar categoria ja inativa retorna 409 | P1 | Negativo | CA-re-desativar | #4 |
| [ ] | CT-018 | Buscar categoria inexistente retorna 404 NOT_FOUND | P1 | Negativo | CA-404-inexistente | #3 |
| [ ] | CT-019 | Buscar categoria de outro usuario retorna 404 NOT_FOUND | P1 | Negativo | CA-ownership | #3 |
| [ ] | CT-020 | Cadastro com campos obrigatorios ausentes retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-campos-ausentes | #5 |
| [ ] | CT-021 | Atualizacao com nome/tipo ausentes retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-campos-ausentes | #5 |
| [ ] | CT-022 | Cadastro com nome maior que 100 caracteres retorna 400 | P2 | Negativo | CA-name-too-long | #5 |
| [ ] | CT-023 | Cadastro com tipo invalido retorna 400 BAD_REQUEST | P2 | Negativo | CA-type-invalido | #5 |
| [ ] | CT-024 | Listar com ?type invalido retorna 400 BAD_REQUEST | P2 | Negativo | CA-type-invalido | #2 |
| [ ] | CT-025 | Criar duas categorias com o mesmo nome e permitido | P2 | Positivo | CA-sem-unicidade | #8 |
| [-] | CT-026 | Sem delete fisico de categoria (endpoint nao existe) | P3 | Normal | - | #5 |
| [-] | CT-027 | Desativacao e terminal (sem endpoint de reativacao) | P3 | Normal | - | #5 |

---

### CT-001 - Contrato do response de sucesso do POST /categories
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/categorias/category-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado via API (`TestUserFixture`)
- **Passos:**
  1. Enviar `POST /categories` com payload válido e único
  2. Validar o corpo (201) contra o schema estrito
- **Dados de entrada:** `CategoryRequest` faker (name, type)
- **Resultado esperado:** HTTP 201; body `{id,name,type,status,createdAt,updatedAt}` conforme schema (`additionalProperties: false`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-002 - Contrato do response de erro do POST /categories
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /categories` sem `name` (400 de validação)
  2. Validar o corpo do erro contra o schema
- **Dados de entrada:** `{"type":"EXPENSE"}` (sem name)
- **Resultado esperado:** HTTP 400; body `{timestamp,status,error,message,path,fields}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-003 - Contrato do response de sucesso do GET /categories
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/categorias/category-list-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com ao menos 1 categoria
- **Passos:**
  1. Enviar `GET /categories`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** sem query params
- **Resultado esperado:** HTTP 200; body é um array de `CategoryResponse` ordenado por nome
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-004 - Contrato do response de erro do GET /categories
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /categories` sem `Authorization`
  2. Validar o corpo do erro (401) contra o schema
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401; body `{timestamp,status,error,message,path}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-005 - Contrato do response de sucesso do GET /categories/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/categorias/category-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com 1 categoria
- **Passos:**
  1. Enviar `GET /categories/{id}` com o id da categoria
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** id de categoria recém-criada
- **Resultado esperado:** HTTP 200; body conforme `schemas/categorias/category-response.json`
- **CA:** - **Regra:** - **Observacoes:** shape idêntico ao do POST (mesmo DTO); mantido por endpoint conforme convenção de contrato

### CT-006 - Contrato do response de erro do GET /categories/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /categories/{uuid-aleatorio}` (inexistente)
  2. Validar o corpo do erro (404) contra o schema
- **Dados de entrada:** UUID aleatório
- **Resultado esperado:** HTTP 404; body `{timestamp,status,error,message,path}` conforme schema (`message` = "Categoria não encontrada")
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-007 - Contrato do response de sucesso do PUT /categories/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/categorias/category-response.json` (a criar)
- **Pre-condicoes:** usuário autenticado com 1 categoria
- **Passos:**
  1. Enviar `PUT /categories/{id}` com novo `name`/`type`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** `CategoryUpdateRequest` válido
- **Resultado esperado:** HTTP 200; body conforme schema com dados atualizados
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-008 - Contrato do response de erro do PUT /categories/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `PUT /categories/{uuid-aleatorio}` com payload sem `type`
  2. Validar o corpo do erro contra o schema
- **Dados de entrada:** body sem campo `type`
- **Resultado esperado:** HTTP 400; body conforme schema com `fields.type` (ou 404 quando o id não existe)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-009 - Contrato do response de sucesso do PATCH /categories/{id}/deactivate
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** n/a (sem corpo — validação de status 204)
- **Pre-condicoes:** usuário autenticado com 1 categoria ativa
- **Passos:**
  1. Enviar `PATCH /categories/{id}/deactivate`
  2. Validar HTTP 204 e corpo vazio
- **Dados de entrada:** id de categoria ativa
- **Resultado esperado:** HTTP 204 No Content; body vazio
- **CA:** - **Regra:** - **Observacoes:** sem schema aplicável (response sem corpo); validar status/headers

### CT-010 - Contrato do response de erro do PATCH /categories/{id}/deactivate
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** uma categoria já desativada
- **Passos:**
  1. Reenviar `PATCH /categories/{id}/deactivate` na categoria inativa
  2. Validar o corpo do erro (409) contra o schema
- **Dados de entrada:** id de categoria INACTIVE
- **Resultado esperado:** HTTP 409; body `{timestamp,status,error,message,path}` conforme schema (`message` = "A categoria já está inativa")
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-011 - Criar categoria com dados validos retorna 201 com status ACTIVE
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário registrado via API (`TestUserFixture`)
- **Passos:**
  1. Enviar `POST /categories` com `name`/`type` válidos
  2. Asserir 201 e campos do `CategoryResponse`
- **Dados de entrada:** categoria EXPENSE com nome faker único
- **Resultado esperado:** HTTP 201; `status` ACTIVE; `type` conforme enviado; `id` retornado
- **CA:** CA-criar - **Regra:** #1
- **Observacoes:** usar `UserFaker.categoryName()`

### CT-012 - Listar categorias retorna array ordenado por nome
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com categorias INCOME e EXPENSE criadas (nomes fora de ordem)
- **Passos:**
  1. Enviar `GET /categories`
  2. Asserir 200 e ordem dos `name`
- **Dados de entrada:** categorias "C","A","B" criadas em sequência
- **Resultado esperado:** HTTP 200; array com names em ordem alfabética
- **CA:** CA-listar - **Regra:** #2
- **Observacoes:** `findByUser_IdOrderByName` (sem `?type`)

### CT-013 - Listar categorias filtrando por tipo (?type=INCOME|EXPENSE)
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com categorias INCOME e EXPENSE
- **Passos:**
  1. Enviar `GET /categories?type=INCOME` (variação 1) e `?type=EXPENSE` (variação 2)
  2. Asserir 200 e conteúdo
- **Dados de entrada:** query param `type`
- **Resultado esperado:** apenas categorias do tipo informado, ordenadas por nome
- **CA:** CA-listar-tipo - **Regra:** #2
- **Observacoes:** combinado na revisão (original tinha CTs separados para INCOME e EXPENSE); caso parametrizado

### CT-014 - Buscar categoria por id retorna a categoria
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com 1 categoria criada
- **Passos:**
  1. Enviar `GET /categories/{id}`
  2. Asserir 200 e campos
- **Dados de entrada:** id da categoria criada
- **Resultado esperado:** HTTP 200; `id`/`name`/`type` conforme a criação
- **CA:** CA-buscar - **Regra:** #3
- **Observacoes:** -

### CT-015 - Atualizar categoria altera nome/tipo
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com categoria EXPENSE
- **Passos:**
  1. Enviar `PUT /categories/{id}` com novo `name` e `type` INCOME
  2. Asserir 200 e buscar a categoria
- **Dados de entrada:** `CategoryUpdateRequest` válido
- **Resultado esperado:** HTTP 200; `name`/`type` atualizados
- **CA:** CA-atualizar - **Regra:** #3
- **Observacoes:** -

### CT-016 - Desativar categoria retorna 204 e status INACTIVE
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com categoria ativa
- **Passos:**
  1. Enviar `PATCH /categories/{id}/deactivate`
  2. Asserir 204 e buscar a categoria
- **Dados de entrada:** id de categoria ACTIVE
- **Resultado esperado:** HTTP 204 sem corpo; `GET /categories/{id}` retorna `status` INACTIVE
- **CA:** CA-desativar - **Regra:** #5
- **Observacoes:** -

### CT-017 - Desativar categoria ja inativa retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** categoria previamente desativada via API
- **Passos:**
  1. Reenviar `PATCH /categories/{id}/deactivate`
  2. Asserir status e mensagem
- **Dados de entrada:** id de categoria INACTIVE
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` ("A categoria já está inativa" — `category.alreadyInactive`)
- **CA:** CA-re-desativar - **Regra:** #4
- **Observacoes:** -

### CT-018 - Buscar categoria inexistente retorna 404 NOT_FOUND
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /categories/{uuid-aleatorio}`
  2. Asserir status e shape de erro
- **Dados de entrada:** UUID aleatório
- **Resultado esperado:** HTTP 404 `NOT_FOUND` ("Categoria não encontrada")
- **CA:** CA-404-inexistente - **Regra:** #3
- **Observacoes:** aplicar também a PUT e deactivate (mesmo `findOwned`)

### CT-019 - Buscar categoria de outro usuario retorna 404 NOT_FOUND
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** dois usuários A e B; A tem categoria
- **Passos:**
  1. Logar como B e enviar `GET /categories/{id-da-categoria-de-A}`
  2. Asserir 404
- **Dados de entrada:** id da categoria de outro usuário
- **Resultado esperado:** HTTP 404 `NOT_FOUND` (ownership por `findByUser_IdAndId` — nunca 403)
- **CA:** CA-ownership - **Regra:** #3
- **Observacoes:** ownership vale para todos os endpoints autenticados

### CT-020 - Cadastro com campos obrigatorios ausentes retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /categories` omitindo um campo obrigatório por vez (name, type)
  2. Asserir 400 e `fields`
- **Dados de entrada:** payloads parametrizados (campo ausente)
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.<campo>` preenchido (`name.required`/`type.required`)
- **CA:** CA-campos-ausentes - **Regra:** #5
- **Observacoes:** caso parametrizado (`deveRejeitarCategoriaSemCampoObrigatorio`)

### CT-021 - Atualizacao com nome/tipo ausentes retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** categoria existente
- **Passos:**
  1. Enviar `PUT /categories/{id}` omitindo `name` (ou `type`)
  2. Asserir 400 e `fields`
- **Dados de entrada:** `CategoryUpdateRequest` incompleto
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`fields.name`/`fields.type`)
- **CA:** CA-campos-ausentes - **Regra:** #5
- **Observacoes:** [SUPOSICAO] mesmo shape do POST (mesmo global handler)

### CT-022 - Cadastro com nome maior que 100 caracteres retorna 400
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /categories` com `name` de 101 caracteres
  2. Asserir 400 e `fields.name`
- **Dados de entrada:** name com 101 caracteres
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`name.tooLong`)
- **CA:** CA-name-too-long - **Regra:** #5
- **Observacoes:** revisão removeu o CT duplicado de `name>100` no PUT (mesma validação)

### CT-023 - Cadastro com tipo invalido retorna 400 BAD_REQUEST
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /categories` com `type: "INVESTIMENTO"`
  2. Asserir 400
- **Dados de entrada:** enum inválido no corpo
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (enum mismatch — sem `fields`)
- **CA:** CA-type-invalido - **Regra:** #5
- **Observacoes:** diferencia do 400 `VALIDATION_ERROR`

### CT-024 - Listar com ?type invalido retorna 400 BAD_REQUEST
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /categories?type=INVALIDO`
  2. Asserir 400
- **Dados de entrada:** query param com enum inválido
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (`MethodArgumentTypeMismatchException`)
- **CA:** CA-type-invalido - **Regra:** #2
- **Observacoes:** -

### CT-025 - Criar duas categorias com o mesmo nome e permitido
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário autenticado
- **Passos:**
  1. Enviar `POST /categories` com nome N (type EXPENSE)
  2. Enviar o mesmo nome N (type INCOME)
  3. Listar e asserir os dois registros
- **Dados de entrada:** mesmo `name` em duas requisições
- **Resultado esperado:** ambas retornam 201 (sem unicidade de nome, sem 409)
- **CA:** CA-sem-unicidade - **Regra:** #8
- **Observacoes:** limitação da app documentada (sem `@UniqueConstraint`); diferença de tipo deixa o caso mais robusto

### CT-026 - Sem delete fisico de categoria (endpoint nao existe)
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Tentar `DELETE /categories/{id}`
  2. Observar resposta
- **Dados de entrada:** id de categoria
- **Resultado esperado:** sem endpoint — 404/405 (fluxo não automatizável via API)
- **CA:** - **Regra:** #5
- **Observacoes:** [-]: não automatizável — não existe `@DeleteMapping` no app

### CT-027 - Desativacao e terminal (sem endpoint de reativacao)
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** API
- **Pre-condicoes:** categoria desativada
- **Passos:**
  1. Procurar endpoint de reativação em `/categories`
  2. Observar resposta para qualquer candidato (GET/POST/PUT/PATCH)
- **Dados de entrada:** id de categoria INACTIVE
- **Resultado esperado:** sem endpoint de reativação — status INACTIVE é terminal
- **CA:** - **Regra:** #5
- **Observacoes:** [-]: não automatizável — sem reativação via API; "terminal" é indireto (CT-017 re-desativar 409)

---