# Casos de Teste API - Faturas

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/faturas/faturas.md

## Resumo Executivo
- **Total de casos de teste:** 21
- **Executados:** 0/21 (0%)
- **Distribuição por prioridade:**
  - P0: 12
  - P1: 7
  - P2: 2
- **Critérios de aceite cobertos:** 14 de 14 (API)
- **Regras de negócio cobertas:** 8/8
- **Observações gerais:** nenhum caso `[x]` — feature nova, sem client/infra
  construídos. Todos os contratos dependem de schema a criar
  (`schemas/faturas/*` e `schemas/common/error-response.json`). Estado
  pré-condição (compra/fatura) criado via `PurchaseClient`/`InvoiceClient`
  (fixture de fatura idempotente). Faturas são **API-only** — o ciclo completo
  (compra → close → pay) é o CT P0 mais importante.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Contrato do response de sucesso do GET /invoices | P0 | Contrato | - | - |
| [ ] | CT-002 | Contrato do response de erro do GET /invoices | P0 | Contrato | - | - |
| [ ] | CT-003 | Contrato do response de sucesso do GET /invoices/{id} | P0 | Contrato | - | - |
| [ ] | CT-004 | Contrato do response de erro do GET /invoices/{id} | P0 | Contrato | - | - |
| [ ] | CT-005 | Contrato do response de sucesso do POST /invoices/{id}/close | P0 | Contrato | - | - |
| [ ] | CT-006 | Contrato do response de erro do POST /invoices/{id}/close | P0 | Contrato | - | - |
| [ ] | CT-007 | Contrato do response de sucesso do POST /invoices/{id}/pay | P0 | Contrato | - | - |
| [ ] | CT-008 | Contrato do response de erro do POST /invoices/{id}/pay | P0 | Contrato | - | - |
| [ ] | CT-009 | Ciclo completo compra, fecha e paga fatura com débito na conta | P0 | Positivo | CA-ciclo | #1,#2,#3,#4,#5,#6 |
| [ ] | CT-010 | Listar faturas ordenadas por referenceMonth DESC | P0 | Positivo | CA-listar | #8 |
| [ ] | CT-011 | Obter detalhe da fatura com installments | P0 | Positivo | CA-detalhe | #7 |
| [ ] | CT-012 | Fechar fatura aberta recalcula total para Σ parcelas | P0 | Positivo | CA-close-feliz | #3 |
| [ ] | CT-013 | Fechar fatura já fechada retorna 409 invoice.onlyOpenCanClose | P1 | Negativo | CA-close-fechada | #3 |
| [ ] | CT-014 | Pagar fatura aberta retorna 409 invoice.mustBeClosedToPay | P1 | Negativo | CA-pay-nao-fechada | #4 |
| [ ] | CT-015 | Pagar fatura já paga retorna 409 invoice.alreadyPaid | P1 | Negativo | CA-pay-paga | #4 |
| [ ] | CT-016 | Pagar com conta inativa retorna 409 account.notActive | P1 | Negativo | CA-pay-conta-inativa | #5 |
| [ ] | CT-017 | Pagar com conta de outro usuário retorna 404 account.notFound | P1 | Negativo | CA-pay-404 | #5,#7 |
| [ ] | CT-018 | Pay sem accountId retorna 400 VALIDATION_ERROR com fields | P1 | Negativo | CA-validacao-accountId | #5 |
| [ ] | CT-019 | Consultar fatura inexistente retorna 404 invoice.notFound | P1 | Negativo | CA-404 | #7 |
| [ ] | CT-020 | Operações em fatura de outro usuário retornam 404 | P2 | Negativo | CA-404 | #7 |
| [ ] | CT-021 | Endpoint de faturas sem token retorna 401 UNAUTHORIZED | P2 | Negativo | CA-401 | #7 |

---

### CT-001 - Contrato do response de sucesso do GET /invoices
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/faturas/invoice-list-response.json` (a criar)
- **Pre-condicoes:** usuário com fatura gerada via compra (`PurchaseFixture`)
- **Passos:**
  1. Enviar `GET /invoices` autenticado
  2. Validar o corpo (Page) contra o schema estrito
- **Dados de entrada:** sem query params (página default)
- **Resultado esperado:** HTTP 200; body `Page<InvoiceResponse>` (content,
  totalElements, totalPages, size, number) respeitando o schema
  (`additionalProperties: false`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas
  (criar `schemas/faturas/invoice-list-response.json`). Coberto por
  `FaturaApiTest#deveValidarContratoDeListagemDeFaturas`

### CT-002 - Contrato do response de erro do GET /invoices
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /invoices` sem `Authorization`
  2. Validar o corpo de erro (401) contra o schema
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401; body `{timestamp,status,error,message,path}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas
  (criar `schemas/common/error-response.json`)

### CT-003 - Contrato do response de sucesso do GET /invoices/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/faturas/invoice-detail-response.json` (a criar)
- **Pre-condicoes:** usuário com fatura gerada via compra
- **Passos:**
  1. Enviar `GET /invoices/{id}` autenticado
  2. Validar o corpo (detalhe + installments) contra o schema estrito
- **Dados de entrada:** id da fatura criada
- **Resultado esperado:** HTTP 200; body `InvoiceDetailResponse` incluindo
  `installments[]` com todos os campos (`additionalProperties: false`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-004 - Contrato do response de erro do GET /invoices/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /invoices/{id}` com UUID inexistente
  2. Validar o corpo de erro (404) contra o schema
- **Dados de entrada:** UUID aleatório
- **Resultado esperado:** HTTP 404; body conforme schema de erro
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-005 - Contrato do response de sucesso do POST /invoices/{id}/close
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/faturas/invoice-response.json` (a criar)
- **Pre-condicoes:** usuário com fatura `OPEN` (via compra)
- **Passos:**
  1. Enviar `POST /invoices/{id}/close` autenticado
  2. Validar o corpo (InvoiceResponse) contra o schema estrito
- **Dados de entrada:** id da fatura aberta
- **Resultado esperado:** HTTP 200; body `InvoiceResponse` com `status: CLOSED`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas;
  mesmo schema usado no pay (CT-007)

### CT-006 - Contrato do response de erro do POST /invoices/{id}/close
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** usuário com fatura já `CLOSED`
- **Passos:**
  1. Enviar `POST /invoices/{id}/close` na fatura fechada
  2. Validar o corpo de erro (409) contra o schema
- **Dados de entrada:** id da fatura fechada
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-007 - Contrato do response de sucesso do POST /invoices/{id}/pay
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/faturas/invoice-response.json` (a criar)
- **Pre-condicoes:** usuário com fatura `CLOSED` e conta `ACTIVE`
- **Passos:**
  1. Enviar `POST /invoices/{id}/pay` com `accountId` autenticado
  2. Validar o corpo (InvoiceResponse) contra o schema estrito
- **Dados de entrada:** `{"accountId": "<conta-ativa>"}`
- **Resultado esperado:** HTTP 200; body com `status: PAID` e `paidAt` preenchido
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-008 - Contrato do response de erro do POST /invoices/{id}/pay
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** usuário com fatura não-fechada
- **Passos:**
  1. Enviar `POST /invoices/{id}/pay` em fatura `OPEN`
  2. Validar o corpo de erro (409) contra o schema
- **Dados de entrada:** `{"accountId": "<conta-ativa>"}`
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-009 - Ciclo completo compra, fecha e paga fatura com débito na conta
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com conta `ACTIVE` e cartão `ACTIVE` (via API)
- **Passos:**
  1. Criar compra parcelada no mês corrente via `POST /purchases`
  2. Buscar a fatura gerada em `GET /invoices` (status `OPEN`)
  3. Fechar a fatura com `POST /invoices/{id}/close`
  4. Pagar com `POST /invoices/{id}/pay` (`accountId` da conta)
  5. Verificar saldo da conta debitado em `totalAmount`
  6. Verificar `GET /invoices/{id}` com installments `PAID` e `paidAt` preenchido
- **Dados de entrada:** massa via `PurchaseBuilder` (total/parcelas dinâmicos)
- **Resultado esperado:** fatura nasce `OPEN` → `CLOSED` (total = Σ parcelas) →
  `PAID`; parcelas `PAID`; saldo da conta reduzido em `totalAmount`
- **CA:** CA-ciclo - **Regra:** #1,#2,#3,#4,#5,#6
- **Observacoes:** coberto por
  `FaturaApiTest#deveExecutarCicloCompletoDeFatura`; é o smoke da feature
  (`@Tag("smoke")`)

### CT-010 - Listar faturas ordenadas por referenceMonth DESC
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com faturas em meses distintos (2+ compras)
- **Passos:**
  1. Enviar `GET /invoices`
  2. Comparar a ordem dos `referenceMonth` do `content`
- **Dados de entrada:** compras em meses diferentes
- **Resultado esperado:** HTTP 200; faturas ordenadas por `referenceMonth` DESC
  (mais recente primeiro)
- **CA:** CA-listar - **Regra:** #8
- **Observacoes:** coberto por `FaturaApiTest#deveListarFaturasPorMesDecrescente`

### CT-011 - Obter detalhe da fatura com installments
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com fatura de compra parcelada
- **Passos:**
  1. Enviar `GET /invoices/{id}`
  2. Validar a lista `installments[]` (número, valor, status, descrição da compra)
- **Dados de entrada:** id da fatura
- **Resultado esperado:** HTTP 200; `installments.length` = nº de parcelas;
  soma dos valores = `totalAmount` da fatura; `status` `OPEN`
- **CA:** CA-detalhe - **Regra:** #7
- **Observacoes:** coberto por `FaturaApiTest#deveObterDetalheDaFaturaComParcelas`

### CT-012 - Fechar fatura aberta recalcula total para Σ parcelas
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário com fatura `OPEN` (via compra)
- **Passos:**
  1. Enviar `POST /invoices/{id}/close`
  2. Conferir `totalAmount` = Σ installments e `status = CLOSED`
- **Dados de entrada:** id da fatura aberta
- **Resultado esperado:** HTTP 200; `totalAmount` recalculado; `status: CLOSED`
- **CA:** CA-close-feliz - **Regra:** #3
- **Observacoes:** total com valor defasado durante vida da fatura é recalculado
  no fechamento (`service/InvoiceService.java:61-64`)

### CT-013 - Fechar fatura já fechada retorna 409 invoice.onlyOpenCanClose
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário com fatura `CLOSED`
- **Passos:**
  1. Enviar `POST /invoices/{id}/close` na fatura fechada
  2. Asserir status e mensagem
- **Dados de entrada:** id da fatura `CLOSED`
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION`,
  `invoice.onlyOpenCanClose`
- **CA:** CA-close-fechada - **Regra:** #3
- **Observacoes:** coberto por `FaturaApiTest#deveRejeitarFechamentoDeFaturaFechada`

### CT-014 - Pagar fatura aberta retorna 409 invoice.mustBeClosedToPay
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário com fatura `OPEN`
- **Passos:**
  1. Enviar `POST /invoices/{id}/pay` com conta válida
  2. Asserir status e mensagem
- **Dados de entrada:** fatura `OPEN` + conta `ACTIVE`
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION`, `invoice.mustBeClosedToPay`
- **CA:** CA-pay-nao-fechada - **Regra:** #4
- **Observacoes:** coberto por `FaturaApiTest#deveRejeitarPagamentoDeFaturaAberta`

### CT-015 - Pagar fatura já paga retorna 409 invoice.alreadyPaid
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário com fatura `PAID`
- **Passos:**
  1. Enviar `POST /invoices/{id}/pay` novamente
  2. Asserir status e mensagem
- **Dados de entrada:** fatura `PAID` + conta `ACTIVE`
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION`, `invoice.alreadyPaid`
- **CA:** CA-pay-paga - **Regra:** #4
- **Observacoes:** coberto por `FaturaApiTest#deveRejeitarPagamentoDuplicado`

### CT-016 - Pagar com conta inativa retorna 409 account.notActive
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário com fatura `CLOSED` e conta `INACTIVE`
- **Passos:**
  1. Enviar `POST /invoices/{id}/pay` com `accountId` da conta inativa
  2. Asserir status e mensagem
- **Dados de entrada:** `{"accountId": "<conta-inativa>"}`
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION`, `account.notActive`
- **CA:** CA-pay-conta-inativa - **Regra:** #5
- **Observacoes:** coberto por `FaturaApiTest#deveRejeitarPagamentoComContaInativa`

### CT-017 - Pagar com conta de outro usuário retorna 404 account.notFound
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário A com fatura `CLOSED`; usuário B com conta
- **Passos:**
  1. Enviar `POST /invoices/{id}/pay` (token A) com `accountId` da conta do B
  2. Asserir status e mensagem
- **Dados de entrada:** `{"accountId": "<conta-de-B>"}`
- **Resultado esperado:** HTTP 404 `NOT_FOUND`, `account.notFound` (ownership)
- **CA:** CA-pay-404 - **Regra:** #5,#7
- **Observacoes:** ownership de conta no pay usa `findByUser_IdAndId`

### CT-018 - Pay sem accountId retorna 400 VALIDATION_ERROR com fields
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário com fatura `CLOSED`
- **Passos:**
  1. Enviar `POST /invoices/{id}/pay` com body vazio ou `accountId: null`
  2. Asserir 400 e `fields.accountId`
- **Dados de entrada:** `{}` ou `{"accountId": null}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.accountId`
- **CA:** CA-validacao-accountId - **Regra:** #5
- **Observacoes:** `@NotNull(message = "accountId.required")` no
  `PayInvoiceRequest`

### CT-019 - Consultar fatura inexistente retorna 404 invoice.notFound
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /invoices/{id}` com UUID aleatório
  2. Asserir status e mensagem
- **Dados de entrada:** UUID aleatório
- **Resultado esperado:** HTTP 404 `NOT_FOUND`, `invoice.notFound`
- **CA:** CA-404 - **Regra:** #7
- **Observacoes:** coberto por `FaturaApiTest#deveRetornar404ParaFaturaInexistente`

### CT-020 - Operações em fatura de outro usuário retornam 404
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário A com fatura; usuário B autenticado
- **Passos:**
  1. Enviar `GET /invoices/{id}` (token B) com id da fatura do A
  2. (parametrizado) também `close` e `pay` na fatura do A
  3. Asserir 404 em todos os casos
- **Dados de entrada:** token do B + id da fatura do A
- **Resultado esperado:** HTTP 404 `NOT_FOUND`, `invoice.notFound` (ownership)
- **CA:** CA-404 - **Regra:** #7
- **Observacoes:** [SUPOSICAO] ownership uniforme via `findByUser_IdAndId`;
  pode ser um caso parametrizado (get/close/pay)

### CT-021 - Endpoint de faturas sem token retorna 401 UNAUTHORIZED
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /invoices` ou `GET /invoices/{id}` sem `Authorization`
  2. Asserir 401
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401 `UNAUTHORIZED`
- **CA:** CA-401 - **Regra:** #7
- **Observacoes:** revisão: mantido apesar de redundante com CT-002 (contrato);
  reforça o fluxo sem depender de schema

---