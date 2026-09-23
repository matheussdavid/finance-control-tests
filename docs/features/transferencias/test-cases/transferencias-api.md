# Casos de Teste API - Transferências

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/transferencias/transferencias.md

## Resumo Executivo
- **Total de casos de teste:** 24
- **Executados:** 0/24 (0%)
- **Distribuição por prioridade:**
  - P0: 9
  - P1: 8
  - P2: 4
  - P3: 3
- **Critérios de aceite cobertos:** 13 de 13 (API) — revisão: CT-017 corrigido (saldo **pode** ficar negativo; não existe regra de saldo insuficiente)
- **Regras de negócio cobertas:** 10/10 — regra 6 coberta pelo CT-017 (borda); regra 10 reforçada pelo CT-024 (id não-UUID → 400)
- **Observações gerais:** todos os contratos dependem de schemas ainda não criados. Revisão: CT-024 (duplicado do CT-022) substituído por gap de `id` **fora do formato UUID** → 400 BAD_REQUEST. Feature nova, nenhum teste automatizado.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Contrato do response de sucesso do POST /transfers | P0 | Contrato | - | - |
| [ ] | CT-002 | Contrato do response de erro do POST /transfers | P0 | Contrato | - | - |
| [ ] | CT-003 | Contrato do response de sucesso do GET /transfers (Page) | P0 | Contrato | - | - |
| [ ] | CT-004 | Contrato do response de erro do GET /transfers | P0 | Contrato | - | - |
| [ ] | CT-005 | Contrato do response de sucesso do GET /transfers/{id} | P0 | Contrato | - | - |
| [ ] | CT-006 | Contrato do response de erro do GET /transfers/{id} | P0 | Contrato | - | - |
| [ ] | CT-007 | Transferência válida debita origem e credita destino | P0 | Positivo | CA-transferencia-feliz | #5 |
| [ ] | CT-008 | Listagem retorna Page ordenado por transferDate DESC | P0 | Positivo | CA-lista-ordenada | #7 |
| [ ] | CT-009 | Busca por id retorna a transferência criada | P0 | Positivo | CA-get-id | #9 |
| [ ] | CT-010 | Mesma conta origem e destino retorna 409 | P1 | Negativo | CA-same-account | #1 |
| [ ] | CT-011 | Conta de origem inativa retorna 409 | P1 | Negativo | CA-origem-inativa | #2 |
| [ ] | CT-012 | Conta de destino inativa retorna 409 | P1 | Negativo | CA-destino-inativa | #3 |
| [ ] | CT-013 | Conta de origem ou destino de outro usuário retorna 404 | P1 | Negativo | CA-ownership | #4 |
| [ ] | CT-014 | Id inexistente na busca retorna 404 | P1 | Negativo | CA-id-inexistente | #9 |
| [ ] | CT-015 | Transferência de outro usuário na busca retorna 404 | P1 | Negativo | CA-ownership-get | #9 |
| [ ] | CT-016 | Campos obrigatórios ausentes retornam 400 VALIDATION_ERROR com fields | P1 | Negativo | CA-campos-ausentes | #10 |
| [ ] | CT-017 | Transferência pode deixar saldo negativo na origem (sem 409) | P1 | Borda | CA-saldo-negativo | #6 |
| [ ] | CT-018 | Amount menor que 0.01 retorna 400 VALIDATION_ERROR | P2 | Negativo | CA-amount-invalido | #10 |
| [ ] | CT-019 | transferDate malformada retorna 400 BAD_REQUEST | P2 | Negativo | CA-data-malformada | #10 |
| [ ] | CT-020 | Description acima de 255 caracteres retorna 400 | P2 | Negativo | CA-desc-longa | #10 |
| [ ] | CT-021 | Endpoint autenticado sem token retorna 401 UNAUTHORIZED | P2 | Negativo | CA-token-ausente | - |
| [-] | CT-022 | [-] Endpoints de update/delete não existem (PUT/DELETE /transfers) | P3 | Borda | CA-sem-update-delete | #8 |
| [-] | CT-023 | [-] GET /transfers não possui filtros além de paginação | P3 | Borda | CA-sem-filtros | #7 |
| [ ] | CT-024 | GET /transfers/{id} com id fora do formato UUID retorna 400 BAD_REQUEST | P3 | Negativo | CA-id-nao-uuid | #10 |

---

### CT-001 - Contrato do response de sucesso do POST /transfers
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/transferencias/transfer-response.json` (a criar)
- **Pre-condicoes:** duas contas ACTIVE do usuário (API)
- **Passos:**
  1. Enviar `POST /transfers` com payload válido
  2. Validar o corpo (201) contra o schema estrito
- **Dados de entrada:** `{sourceAccountId, destinationAccountId, amount, transferDate}`
- **Resultado esperado:** HTTP 201; body `{id, sourceAccountId, sourceAccountName, destinationAccountId, destinationAccountName, amount, transferDate, description, createdAt}` conforme schema (`additionalProperties: false`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas; método sugerido: `deveValidarContratoDeSucessoAoCriarTransferencia`

### CT-002 - Contrato do response de erro do POST /transfers
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /transfers` com payload inválido (ex.: amount 0)
  2. Validar o corpo de erro (400) contra o schema
- **Dados de entrada:** `{"sourceAccountId": null}`
- **Resultado esperado:** HTTP 400; body `{timestamp,status,error,message,path,fields}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-003 - Contrato do response de sucesso do GET /transfers (Page)
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/transferencias/list-response.json` (a criar)
- **Pre-condicoes:** pelo menos 1 transferência criada
- **Passos:**
  1. Enviar `GET /transfers`
  2. Validar o corpo (200) contra o schema de `Page`
- **Dados de entrada:** sem query params
- **Resultado esperado:** HTTP 200; body `{content:[TransferResponse], pageable, totalElements, totalPages, number, size,...}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-004 - Contrato do response de erro do GET /transfers
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /transfers` **sem** `Authorization`
  2. Validar o corpo de erro (401) contra o schema
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401 `UNAUTHORIZED`; body conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-005 - Contrato do response de sucesso do GET /transfers/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/transferencias/transfer-response.json` (a criar)
- **Pre-condicoes:** 1 transferência criada (id conhecido)
- **Passos:**
  1. Enviar `GET /transfers/{id}`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** `id` da transferência
- **Resultado esperado:** HTTP 200; mesmo shape do CT-001
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-006 - Contrato do response de erro do GET /transfers/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /transfers/{id}` com id inexistente
  2. Validar o corpo de erro (404) contra o schema
- **Dados de entrada:** `id` aleatório (UUID v4)
- **Resultado esperado:** HTTP 404 `NOT_FOUND` (`transfer.notFound`); body conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-007 - Transferência válida debita origem e credita destino
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** duas contas ACTIVE com saldo inicial conhecido via API
- **Passos:**
  1. Enviar `POST /transfers` com origem≠destino, amount e data válidos
  2. Asserir 201 e campos do response
  3. Consultar os saldos (`GET /accounts`) e comparar
- **Dados de entrada:** faker (amount, data) + contas criadas via fixture
- **Resultado esperado:** HTTP 201; saldo da origem diminui e o da destino aumenta em `amount`
- **CA:** CA-transferencia-feliz - **Regra:** #5
- **Observacoes:** método sugerido: `deveCriarTransferenciaDebitandoOrigemECreditandoDestino`

### CT-008 - Listagem retorna Page ordenado por transferDate DESC
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** 2+ transferências em datas diferentes
- **Passos:**
  1. Enviar `GET /transfers`
  2. Asserir 200 e ordem de `content` por `transferDate` desc
- **Dados de entrada:** sem query
- **Resultado esperado:** HTTP 200; `transferDate` não-crescente; metadados de Page presentes
- **CA:** CA-lista-ordenada - **Regra:** #7
- **Observacoes:** método sugerido: `deveListarTransferenciasOrdenadasPorDataDescendente`

### CT-009 - Busca por id retorna a transferência criada
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** 1 transferência criada
- **Passos:**
  1. Enviar `GET /transfers/{id}` com o id do 201
  2. Asserir 200 e igualdade de campos
- **Dados de entrada:** `id` obtido no create
- **Resultado esperado:** HTTP 200; body idêntico ao do create (exceto timestamps)
- **CA:** CA-get-id - **Regra:** #9
- **Observacoes:** método sugerido: `deveBuscarTransferenciaPorId`

### CT-010 - Mesma conta origem e destino retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** 1 conta ACTIVE
- **Passos:**
  1. Enviar `POST /transfers` com `sourceAccountId == destinationAccountId`
  2. Asserir 409 e mensagem
  3. Conferir que nenhum saldo mudou
- **Dados de entrada:** mesmo uuid nos dois campos
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` (`transfer.sameAccount`)
- **CA:** CA-same-account - **Regra:** #1
- **Observacoes:** -

### CT-011 - Conta de origem inativa retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** origem INACTIVE (via `PATCH /accounts/{id}/deactivate`); destino ACTIVE
- **Passos:**
  1. Enviar `POST /transfers`
  2. Asserir 409
- **Dados de entrada:** origem inativa
- **Resultado esperado:** HTTP 409 (`transfer.sourceNotActive`)
- **CA:** CA-origem-inativa - **Regra:** #2
- **Observacoes:** -

### CT-012 - Conta de destino inativa retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** origem ACTIVE; destino INACTIVE
- **Passos:**
  1. Enviar `POST /transfers`
  2. Asserir 409
- **Dados de entrada:** destino inativa
- **Resultado esperado:** HTTP 409 (`transfer.destinationNotActive`)
- **CA:** CA-destino-inativa - **Regra:** #3
- **Observacoes:** -

### CT-013 - Conta de origem ou destino de outro usuário retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário de apoio com conta (via API); usuário principal autenticado
- **Passos:**
  1. Enviar `POST /transfers` com `sourceAccountId` da conta do outro usuário
  2. Asserir 404
  3. Repetir com `destinationAccountId` do outro usuário
- **Dados de entrada:** parametrizado origem/destino
- **Resultado esperado:** HTTP 404 `NOT_FOUND` (`account.sourceNotFound`/`account.destinationNotFound`)
- **CA:** CA-ownership - **Regra:** #4
- **Observacoes:** parametrizado; nunca 403

### CT-014 - Id inexistente na busca retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /transfers/{id}` com id aleatório
  2. Asserir 404
- **Dados de entrada:** UUID v4 aleatório
- **Resultado esperado:** HTTP 404 (`transfer.notFound`)
- **CA:** CA-id-inexistente - **Regra:** #9
- **Observacoes:** -

### CT-015 - Transferência de outro usuário na busca retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário de apoio cria transferência; usuário principal usa o id
- **Passos:**
  1. Enviar `GET /transfers/{id}` com id da transferência do outro usuário
  2. Asserir 404
- **Dados de entrada:** id do outro usuário
- **Resultado esperado:** HTTP 404 `NOT_FOUND` (`transfer.notFound`)
- **CA:** CA-ownership-get - **Regra:** #9
- **Observacoes:** -

### CT-016 - Campos obrigatórios ausentes retornam 400 VALIDATION_ERROR com fields
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /transfers` omitindo um campo obrigatório por vez (sourceAccountId, destinationAccountId, amount, transferDate)
  2. Asserir 400 e `fields` apontando o campo
- **Dados de entrada:** payloads parametrizados
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.<campo>` (mensagens `*.required`)
- **CA:** CA-campos-ausentes - **Regra:** #10
- **Observacoes:** parametrizado

### CT-017 - Transferência pode deixar saldo negativo na origem (sem 409)
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** origem com saldo inicial 0; amount > saldo
- **Passos:**
  1. Enviar `POST /transfers` com amount > saldo da origem
  2. Asserir 201 e conferir o saldo da origem
- **Dados de entrada:** `amount = 100`, saldo origem = 0
- **Resultado esperado:** HTTP 201; saldo da origem = **-100** (o app **não** valida saldo insuficiente)
- **CA:** CA-saldo-negativo - **Regra:** #6
- **Observacoes:** corrigido na revisão: mantinha "409 por saldo insuficiente" (regra inexistente no app); método sugerido: `devePermitirTransferenciaDeixandoSaldoNegativo`

### CT-018 - Amount menor que 0.01 retorna 400 VALIDATION_ERROR
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** duas contas ACTIVE
- **Passos:**
  1. Enviar `POST /transfers` com `amount = 0` ou `-1`
  2. Asserir 400
- **Dados de entrada:** `amount: 0`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`amount.greaterThanZero`)
- **CA:** CA-amount-invalido - **Regra:** #10
- **Observacoes:** -

### CT-019 - transferDate malformada retorna 400 BAD_REQUEST
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** duas contas ACTIVE
- **Passos:**
  1. Enviar `POST /transfers` com `transferDate: "12/09/2026"`
  2. Asserir 400
- **Dados de entrada:** data fora de `yyyy-MM-dd`
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (parse de LocalDate)
- **CA:** CA-data-malformada - **Regra:** #10
- **Observacoes:** -

### CT-020 - Description acima de 255 caracteres retorna 400
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** duas contas ACTIVE
- **Passos:**
  1. Enviar `POST /transfers` com `description` de 256 caracteres
  2. Asserir 400
- **Dados de entrada:** string 256 chars
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`description.tooLong`)
- **CA:** CA-desc-longa - **Regra:** #10
- **Observacoes:** -

### CT-021 - Endpoint autenticado sem token retorna 401 UNAUTHORIZED
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /transfers` sem `Authorization`
  2. Asserir 401
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401 `UNAUTHORIZED`
- **CA:** CA-token-ausente - **Regra:** -
- **Observacoes:** rotas públicas são só `/auth/*`, Swagger e `/error`

### CT-022 - [-] Endpoints de update/delete não existem (PUT/DELETE /transfers)
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Verificar que não há `@PutMapping`/`@PatchMapping`/`@DeleteMapping` em `TransferController`
  2. (Opcional) enviar `PUT /transfers/{id}` e registrar 404/405
- **Dados de entrada:** -
- **Resultado esperado:** recurso imutável; sem rota de edição/exclusão
- **CA:** CA-sem-update-delete - **Regra:** #8
- **Observacoes:** [ - ] limitação do app — não automatizável como fluxo de sucesso

### CT-023 - [-] GET /transfers não possui filtros além de paginação
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Inspecionar o controller (sem `@RequestParam` de filtro)
  2. (Opcional) chamar `GET /transfers?type=` e registrar ausência de efeito/400
- **Dados de entrada:** -
- **Resultado esperado:** apenas `page`/`size`; sem filtros por tipo/período/conta
- **CA:** CA-sem-filtros - **Regra:** #7
- **Observacoes:** [ - ] limitação do app — controle feito por inspeção de código

### CT-024 - GET /transfers/{id} com id fora do formato UUID retorna 400 BAD_REQUEST
- **Prioridade:** P3
- **Tipo:** Negativo
- **Camada:** API
- **Schema:** `-`
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /transfers/nao-e-uuid`
  2. Asserir 400
- **Dados de entrada:** `id` não-UUID na URL
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (`MethodArgumentTypeMismatchException` → `badRequest.malformed`)
- **CA:** CA-id-nao-uuid - **Regra:** #10
- **Observacoes:** adicionado na revisão (gap de cobertura; substitui o duplicado do CT-022); método sugerido: `deveRetornarBadRequestParaIdForaDoFormatoUuid`

---