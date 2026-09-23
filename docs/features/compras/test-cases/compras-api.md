# Casos de Teste API - Compras (cartão parcelado)

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/compras/compras.md

## Resumo Executivo
- **Total de casos de teste:** 31
- **Executados:** 0/31 (0%)
- **Distribuição por prioridade:**
  - P0: 10
  - P1: 11
  - P2: 7
  - P3: 3
- **Critérios de aceite cobertos:** 11 de 11 (API) após revisão — incluído CA-clamp-dia (CT-031)
- **Regras de negócio cobertas:** 11/11 — regra 7 corrigida (fatura fechada → 409 `purchase.cannotAddToInvoice`, não 404)
- **Observações gerais:** fatura é criada automaticamente; pré-condições usam
  `POST /invoices/{id}/close`. Revisão: CT-031 (duplicado do CT-029) substituído por
  gap de clamp de dia (fevereiro); CT-030 marcado `[-]`. Feature nova, nenhum teste automatizado.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Contrato do response de sucesso do POST /purchases | P0 | Contrato | - | - |
| [ ] | CT-002 | Contrato do response de erro do POST /purchases | P0 | Contrato | - | - |
| [ ] | CT-003 | Contrato do response de sucesso do GET /purchases (Page) | P0 | Contrato | - | - |
| [ ] | CT-004 | Contrato do response de erro do GET /purchases | P0 | Contrato | - | - |
| [ ] | CT-005 | Contrato do response de sucesso do GET /purchases/{id} | P0 | Contrato | - | - |
| [ ] | CT-006 | Contrato do response de erro do GET /purchases/{id} | P0 | Contrato | - | - |
| [ ] | CT-007 | Compra à vista cria purchase com 1 parcela OPEN | P0 | Positivo | CA-compra-vista | #8 |
| [ ] | CT-008 | Compra parcelada divide com DOWN e resto na última parcela | P0 | Positivo | CA-parcelas | #4 |
| [ ] | CT-009 | Listagem retorna Page ordenado por purchaseDate DESC | P0 | Positivo | CA-lista-compras | #10 |
| [ ] | CT-010 | Busca por id retorna purchase com installments | P0 | Positivo | CA-get-purchase | #9 |
| [ ] | CT-011 | Compra com day <= closingDay gera 1ª fatura no mês da compra | P1 | Positivo | CA-primeira-fatura-mes | #5 |
| [ ] | CT-012 | Compra com day > closingDay gera 1ª fatura no mês seguinte | P1 | Positivo | CA-primeira-fatura-seguinte | #5 |
| [ ] | CT-013 | Total acima do limite disponível retorna 409 | P1 | Negativo | CA-limite-insuficiente | #3 |
| [ ] | CT-014 | Cartão inativo retorna 409 | P1 | Negativo | CA-cartao-inativo | #1 |
| [ ] | CT-015 | Cartão inexistente ou de outro usuário retorna 404 | P1 | Negativo | CA-cartao-404 | #1 |
| [ ] | CT-016 | Categoria inativa retorna 409 | P1 | Negativo | CA-cat-inativa | #2 |
| [ ] | CT-017 | Categoria INCOME retorna 409 | P1 | Negativo | CA-cat-income | #2 |
| [ ] | CT-018 | Categoria inexistente ou de outro usuário retorna 404 | P1 | Negativo | CA-cat-404 | #2 |
| [ ] | CT-019 | Compra para fatura já fechada retorna 409 (purchase.cannotAddToInvoice) | P1 | Negativo | CA-fatura-fechada | #7 |
| [ ] | CT-020 | Parcelas nascem OPEN e comprometem usedLimit | P1 | Positivo | CA-parcelas-open | #8 |
| [ ] | CT-021 | Purchase id inexistente ou de outro usuário retorna 404 | P1 | Negativo | CA-purchase-404 | #9 |
| [ ] | CT-022 | Campos obrigatórios ausentes retornam 400 VALIDATION_ERROR | P2 | Negativo | CA-campos-ausentes | #11 |
| [ ] | CT-023 | TotalAmount menor ou igual a 0 retorna 400 | P2 | Negativo | CA-total-invalido | #11 |
| [ ] | CT-024 | InstallmentsCount menor que 1 retorna 400 | P2 | Negativo | CA-parcelas-invalido | #11 |
| [ ] | CT-025 | purchaseDate malformada retorna 400 BAD_REQUEST | P2 | Negativo | CA-data-malformada | #11 |
| [ ] | CT-026 | Description acima de 255 retorna 400 | P2 | Negativo | CA-desc-longa | #11 |
| [ ] | CT-027 | JSON malformado retorna 400 BAD_REQUEST | P2 | Negativo | CA-json-malformado | #11 |
| [ ] | CT-028 | Endpoint autenticado sem token retorna 401 | P2 | Negativo | CA-token-ausente | - |
| [-] | CT-029 | [-] Endpoints de update/delete de compra não existem | P3 | Borda | CA-sem-update-delete | #10 |
| [-] | CT-030 | [-] GET /purchases não possui filtros além de paginação | P3 | Borda | CA-sem-filtros | #10 |
| [ ] | CT-031 | Clamp de dia: compra com dueDay 31 em fevereiro fecha fatura no último dia (28/29) | P3 | Borda | CA-clamp-dia | #6 |

---

### CT-001 - Contrato do response de sucesso do POST /purchases
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/compras/purchase-response.json` (a criar)
- **Pre-condicoes:** cartão ACTIVE + categoria EXPENSE ACTIVE (API)
- **Passos:**
  1. Enviar `POST /purchases` com payload válido
  2. Validar o corpo (201) contra o schema estrito
- **Dados de entrada:** `{creditCardId, categoryId, description, totalAmount, installmentsCount, purchaseDate}`
- **Resultado esperado:** HTTP 201; body `{id, creditCardId, creditCardName, categoryId, categoryName, description, totalAmount, installmentsCount, purchaseDate, createdAt, installments[]}`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas; método sugerido: `deveValidarContratoDeSucessoAoCriarCompra`

### CT-002 - Contrato do response de erro do POST /purchases
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /purchases` com payload inválido (ex.: totalAmount 0)
  2. Validar o corpo de erro (400) contra o schema
- **Dados de entrada:** `{"totalAmount": 0}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` + `fields` conforme schema
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-003 - Contrato do response de sucesso do GET /purchases (Page)
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/compras/list-response.json` (a criar)
- **Pre-condicoes:** 1+ compra criada
- **Passos:**
  1. Enviar `GET /purchases`
  2. Validar o corpo (200) contra o schema de `Page`
- **Dados de entrada:** sem query
- **Resultado esperado:** HTTP 200; `{content:[PurchaseResponse], totalElements, totalPages, number, size,...}`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-004 - Contrato do response de erro do GET /purchases
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /purchases` **sem** token
  2. Validar o corpo (401) contra o schema
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401 `UNAUTHORIZED`
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-005 - Contrato do response de sucesso do GET /purchases/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/compras/purchase-response.json` (a criar)
- **Pre-condicoes:** 1 compra criada
- **Passos:**
  1. Enviar `GET /purchases/{id}`
  2. Validar o corpo (200) contra o schema estrito
- **Dados de entrada:** `id` da compra
- **Resultado esperado:** HTTP 200; mesmo shape do CT-001
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-006 - Contrato do response de erro do GET /purchases/{id}
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json` (a criar)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /purchases/{id}` com id inexistente
  2. Validar o corpo (404) contra o schema
- **Dados de entrada:** UUID v4 aleatório
- **Resultado esperado:** HTTP 404 `NOT_FOUND` (`purchase.notFound`)
- **CA:** - **Regra:** - **Observacoes:** depende da infraestrutura de schemas

### CT-007 - Compra à vista cria purchase com 1 parcela OPEN
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** cartão ACTIVE + categoria EXPENSE ACTIVE
- **Passos:**
  1. Enviar `POST /purchases` com `installmentsCount=1`
  2. Asserir 201, 1 installment `OPEN`, amount = totalAmount
- **Dados de entrada:** totalAmount=100, parcelas=1
- **Resultado esperado:** HTTP 201; `installments.length == 1`
- **CA:** CA-compra-vista - **Regra:** #8
- **Observacoes:** método sugerido: `deveCriarCompraAVistaComParcelaUnica`

### CT-008 - Compra parcelada divide com DOWN e resto na última parcela
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** cartão ACTIVE com limite folgado + categoria EXPENSE
- **Passos:**
  1. Enviar `POST /purchases` com `totalAmount=100`, `installmentsCount=3`
  2. Asserir amounts por installment
- **Dados de entrada:** 100/3
- **Resultado esperado:** HTTP 201; parcelas **33.33 / 33.33 / 33.34** (resto na última, soma 100)
- **CA:** CA-parcelas - **Regra:** #4
- **Observacoes:** método sugerido: `deveParcelarCompraComRestoNaUltimaParcela`

### CT-009 - Listagem retorna Page ordenado por purchaseDate DESC
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** 2+ compras em datas distintas
- **Passos:**
  1. Enviar `GET /purchases`
  2. Asserir 200 e ordem de `content`
- **Dados de entrada:** sem query
- **Resultado esperado:** HTTP 200; `purchaseDate` não-crescente; metadados de Page
- **CA:** CA-lista-compras - **Regra:** #10
- **Observacoes:** método sugerido: `deveListarComprasOrdenadasPorDataDescendente`

### CT-010 - Busca por id retorna purchase com installments
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** 1 compra criada
- **Passos:**
  1. Enviar `GET /purchases/{id}`
  2. Asserir 200 e `installments`
- **Dados de entrada:** `id` do create
- **Resultado esperado:** HTTP 200; `installments[]` completos (invoiceId, referenceMonth, number, amount, status)
- **CA:** CA-get-purchase - **Regra:** #9
- **Observacoes:** -

### CT-011 - Compra com day <= closingDay gera 1ª fatura no mês da compra
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** cartão com `closingDay` conhecido (ex.: 10)
- **Passos:**
  1. Enviar compra com `purchaseDate.day <= closingDay`
  2. Asserir `installments[0].referenceMonth` == mês da compra
- **Dados de entrada:** purchaseDate = 05/09 (closing 10)
- **Resultado esperado:** `referenceMonth` do 1º installment = 2026-09
- **CA:** CA-primeira-fatura-mes - **Regra:** #5
- **Observacoes:** -

### CT-012 - Compra com day > closingDay gera 1ª fatura no mês seguinte
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** cartão com `closingDay` conhecido (ex.: 10)
- **Passos:**
  1. Enviar compra com `purchaseDate.day > closingDay`
  2. Asserir `installments[0].referenceMonth` == mês seguinte
- **Dados de entrada:** purchaseDate = 20/09 (closing 10)
- **Resultado esperado:** `referenceMonth` do 1º installment = 2026-10
- **CA:** CA-primeira-fatura-seguinte - **Regra:** #5
- **Observacoes:** -

### CT-013 - Total acima do limite disponível retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** cartão ACTIVE com limite baixo (sem parcelas OPEN)
- **Passos:**
  1. Enviar `POST /purchases` com `totalAmount > creditLimit - usedLimit`
  2. Asserir 409
- **Dados de entrada:** totalAmount > creditLimit
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` (`purchase.insufficientLimit`)
- **CA:** CA-limite-insuficiente - **Regra:** #3
- **Observacoes:** nada é criado (transactional)

### CT-014 - Cartão inativo retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** cartão INACTIVE (via `PATCH /credit-cards/{id}/deactivate`)
- **Passos:**
  1. Enviar `POST /purchases` apontando para o cartão inativo
  2. Asserir 409
- **Dados de entrada:** creditCardId inativo
- **Resultado esperado:** HTTP 409 `creditCard.notActive`
- **CA:** CA-cartao-inativo - **Regra:** #1
- **Observacoes:** -

### CT-015 - Cartão inexistente ou de outro usuário retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** (opcional) usuário de apoio com cartão
- **Passos:**
  1. Enviar `POST /purchases` com `creditCardId` inexistente/de outro usuário
  2. Asserir 404
- **Dados de entrada:** parametrizado
- **Resultado esperado:** HTTP 404 `creditCard.notFound`
- **CA:** CA-cartao-404 - **Regra:** #1
- **Observacoes:** ownership — nunca 403

### CT-016 - Categoria inativa retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** cartão ACTIVE + categoria EXPENSE INACTIVE
- **Passos:**
  1. Enviar `POST /purchases`
  2. Asserir 409
- **Dados de entrada:** categoryId inativa
- **Resultado esperado:** HTTP 409 `category.notActive`
- **CA:** CA-cat-inativa - **Regra:** #2
- **Observacoes:** -

### CT-017 - Categoria INCOME retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** cartão ACTIVE + categoria INCOME ACTIVE
- **Passos:**
  1. Enviar `POST /purchases`
  2. Asserir 409
- **Dados de entrada:** categoryId INCOME
- **Resultado esperado:** HTTP 409 `category.mustBeExpenseForPurchase`
- **CA:** CA-cat-income - **Regra:** #2
- **Observacoes:** -

### CT-018 - Categoria inexistente ou de outro usuário retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** cartão ACTIVE
- **Passos:**
  1. Enviar `POST /purchases` com `categoryId` inexistente/de outro usuário
  2. Asserir 404
- **Dados de entrada:** parametrizado
- **Resultado esperado:** HTTP 404 `category.notFound`
- **CA:** CA-cat-404 - **Regra:** #2
- **Observacoes:** -

### CT-019 - Compra para fatura já fechada retorna 409 (purchase.cannotAddToInvoice)
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Schema:** `error`
- **Pre-condicoes:** compra → fatura OPEN; depois `POST /invoices/{id}/close`
- **Passos:**
  1. Criar compra cuja 1ª fatura cai no mês M
  2. Fechar a fatura do mês M (`POST /invoices/{id}/close`)
  3. Criar nova compra com 1ª parcela no mês M
  4. Asserir 409
- **Dados de entrada:** mesma `referenceMonth`
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` com `purchase.cannotAddToInvoice` (pt-BR)
- **CA:** CA-fatura-fechada - **Regra:** #7
- **Observacoes:** corrigido na revisão: status real é **409** `BUSINESS_RULE_VIOLATION` (`InvoiceServiceImpl`), não 404 — regra de negócio, não recurso inexistente

### CT-020 - Parcelas nascem OPEN e comprometem usedLimit
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** cartão ACTIVE limpo
- **Passos:**
  1. Criar compra de valor V em 2x
  2. Consultar `GET /credit-cards/{id}`
  3. Asserir `usedLimit == V` (todas as parcelas OPEN)
- **Dados de entrada:** V=100
- **Resultado esperado:** `usedLimit=100`; `availableLimit = creditLimit - 100`
- **CA:** CA-parcelas-open - **Regra:** #8
- **Observacoes:** limite comprometido no momento da criação

### CT-021 - Purchase id inexistente ou de outro usuário retorna 404
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** (opcional) usuário de apoio com compra
- **Passos:**
  1. Enviar `GET /purchases/{id}` com id inexistente/de outro usuário
  2. Asserir 404
- **Dados de entrada:** parametrizado
- **Resultado esperado:** HTTP 404 `purchase.notFound`
- **CA:** CA-purchase-404 - **Regra:** #9
- **Observacoes:** -

### CT-022 - Campos obrigatórios ausentes retornam 400 VALIDATION_ERROR
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /purchases` omitindo um campo por vez (creditCardId, categoryId, totalAmount, installmentsCount, purchaseDate)
  2. Asserir 400 e `fields`
- **Dados de entrada:** payloads parametrizados
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`*.required`)
- **CA:** CA-campos-ausentes - **Regra:** #11
- **Observacoes:** parametrizado

### CT-023 - TotalAmount menor ou igual a 0 retorna 400
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /purchases` com `totalAmount: 0`
  2. Asserir 400
- **Dados de entrada:** `totalAmount: 0`
- **Resultado esperado:** HTTP 400 (`totalAmount.greaterThanZero`)
- **CA:** CA-total-invalido - **Regra:** #11
- **Observacoes:** -

### CT-024 - InstallmentsCount menor que 1 retorna 400
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /purchases` com `installmentsCount: 0`
  2. Asserir 400
- **Dados de entrada:** `installmentsCount: 0`
- **Resultado esperado:** HTTP 400 (`installmentsCount.atLeastOne`)
- **CA:** CA-parcelas-invalido - **Regra:** #11
- **Observacoes:** -

### CT-025 - purchaseDate malformada retorna 400 BAD_REQUEST
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /purchases` com `purchaseDate: "05/09/2026"`
  2. Asserir 400
- **Dados de entrada:** data fora de `yyyy-MM-dd`
- **Resultado esperado:** HTTP 400 `BAD_REQUEST`
- **CA:** CA-data-malformada - **Regra:** #11
- **Observacoes:** -

### CT-026 - Description acima de 255 retorna 400
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /purchases` com `description` de 256 caracteres
  2. Asserir 400
- **Dados de entrada:** string 256 chars
- **Resultado esperado:** HTTP 400 (`description.tooLong`)
- **CA:** CA-desc-longa - **Regra:** #11
- **Observacoes:** -

### CT-027 - JSON malformado retorna 400 BAD_REQUEST
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /purchases` com corpo inválido
  2. Asserir 400
- **Dados de entrada:** `{` (inválido)
- **Resultado esperado:** HTTP 400 `BAD_REQUEST` (`badRequest.malformed`)
- **CA:** CA-json-malformado - **Regra:** #11
- **Observacoes:** -

### CT-028 - Endpoint autenticado sem token retorna 401
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /purchases` sem `Authorization`
  2. Asserir 401
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401 `UNAUTHORIZED`
- **CA:** CA-token-ausente - **Regra:** -
- **Observacoes:** -

### CT-029 - [-] Endpoints de update/delete de compra não existem
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Verificar `PurchaseController`: só POST/GET; sem PUT/DELETE
- **Dados de entrada:** -
- **Resultado esperado:** compra imutável
- **CA:** CA-sem-update-delete - **Regra:** #10
- **Observacoes:** [ - ] limitação do app

### CT-030 - [-] GET /purchases não possui filtros além de paginação
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Inspecionar `PurchaseController.list` (sem filtros)
- **Dados de entrada:** -
- **Resultado esperado:** apenas `page`/`size`; sort `purchaseDate DESC`
- **CA:** CA-sem-filtros - **Regra:** #10
- **Observacoes:** [ - ] limitação do app

### CT-031 - Clamp de dia: compra com dueDay 31 em fevereiro fecha fatura no último dia (28/29)
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** cartão com closingDay/dueDay 31; compra em fevereiro
- **Passos:**
  1. Registrar compra cuja fatura em fevereiro tenha dia 31
  2. Consultar a fatura (`GET /invoices` ou parcelas)
- **Dados de entrada:** referencia a dia 31 em mês de 28/29 dias
- **Resultado esperado:** fatura criada com dia clampsado para 28/29 (fevereiro) via `withDayClamped`; sem falha
- **CA:** CA-clamp-dia - **Regra:** #6
- **Observacoes:** adicionado na revisão (gap de cobertura; substitui o duplicado do CT-029); método sugerido: `deveAplicarClampDeDiaParaUltimoDiaDeFevereiro`

---