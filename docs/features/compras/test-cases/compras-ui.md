# Casos de Teste UI - Compras (cartão parcelado)

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/compras/compras.md

## Resumo Executivo
- **Total de casos de teste:** 12
- **Executados:** 0/12 (0%)
- **Distribuição por prioridade:**
  - P0: 2
  - P1: 5
  - P2: 3
  - P3: 2
- **Critérios de aceite cobertos:** 9 de 9 (UI) após revisão — incluídos CAs de sem-fatura (CT-011) e sem-edição/exclusão (CT-012)
- **Regras de negócio cobertas:** 8/8 — CT-002 corrigido (resumo **atualiza** após a compra)
- **Observações gerais:** fluxo de compra vive em `/cartoes` (`credit-cards-purchase-form`);
  não há rota própria de compras nem tela de fatura (InvoicesPage órfã).
  Revisão: adicionados CT-011/CT-012 (limitações); CT-002 corrigido. Feature nova, nenhum teste automatizado.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Registrar compra válida exibe item na lista e sucesso com nº de parcelas | P0 | Positivo | CA-compra-ui-feliz | #3 |
| [ ] | CT-002 | Resumo used/available atualiza após a compra registrada | P0 | Positivo | CA-resumo-pos-compra | #4 |
| [ ] | CT-003 | Valor acima do disponível é criado com sucesso | P1 | Positivo | CA-limite-aceito | #4 |
| [ ] | CT-004 | Campos obrigatórios vazios impedem submit | P1 | Negativo | CA-required | #5 |
| [ ] | CT-005 | Select de cartão lista apenas cartões ACTIVE | P1 | Positivo | CA-select-cartao | #2 |
| [ ] | CT-006 | Select de categoria lista apenas EXPENSE ACTIVE | P1 | Positivo | CA-select-categoria | #2 |
| [ ] | CT-007 | Compra registrada aparece na lista com cartão/categoria/Nx | P1 | Positivo | CA-item-lista | #3 |
| [ ] | CT-008 | Lista de compras vazia exibe empty state | P2 | Borda | CA-compras-vazias | #6 |
| [ ] | CT-009 | Data futura é aceita no form de compra | P2 | Borda | CA-data-futura | #8 |
| [ ] | CT-010 | Resumo used/available permanece inalterado após a compra | P2 | Positivo | CA-resumo-negativo | #4 |
| [-] | CT-011 | [-] Ver/fechar/pagar fatura pela UI não existe (InvoicesPage órfã) | P3 | Borda | CA-sem-tela-fatura | #9 |
| [-] | CT-012 | [-] Editar/excluir compra pela UI não existe | P3 | Borda | CA-sem-edicao-compra | #10 |

---

### CT-001 - Registrar compra válida exibe item na lista e sucesso com nº de parcelas
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** cartão ACTIVE + categoria EXPENSE ACTIVE (via API); tela `/cartoes`
- **Passos:**
  1. Preencher `credit-cards-purchase-form` (card, category, amount, date, installments=3)
  2. Clicar em `credit-cards-purchase-submit-btn`
- **Dados de entrada:** compra via form
- **Resultado esperado:** `message-success` com `cards.purchaseRegistered` (n=3); form reseta mantendo o cartão
- **CA:** CA-compra-ui-feliz - **Regra:** #3
- **Observacoes:** método esperado: `deveRegistrarCompraPelaInterface`

### CT-002 - Resumo used/available atualiza após a compra registrada
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** cartão ACTIVE com limite conhecido; `/cartoes`
- **Passos:**
  1. Anotar `credit-cards-summary-used`/`-available`
  2. Registrar compra via `credit-cards-purchase-form`
  3. Reobservar resumo
- **Dados de entrada:** compra de valor V
- **Resultado esperado:** `used` **aumenta** V e `available` **diminui** V após o sucesso (resumo recalcula no refetch)
- **CA:** CA-resumo-pos-compra - **Regra:** #4
- **Observacoes:** corrigido na revisão: o resumo **atualiza** após a compra (não permanece inalterado)

### CT-003 - Valor acima do disponível é criado com sucesso
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** cartão ACTIVE com limite baixo + categoria EXPENSE
- **Passos:**
  1. Preencher `credit-cards-purchase-amount-input` acima do disponível
  2. Submeter
- **Dados de entrada:** totalAmount > availableLimit
- **Resultado esperado:** `message-success`; item `credit-cards-purchase-{id}` criado
- **CA:** CA-limite-aceito - **Regra:** #4
- **Observacoes:** [SUPOSICAO] comportamento — conferir no app ao automatizar

### CT-004 - Campos obrigatórios vazios impedem submit
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** `/cartoes`
- **Passos:**
  1. Submeter `credit-cards-purchase-form` sem cartão/categoria/valor/data/parcelas
- **Dados de entrada:** form vazio
- **Resultado esperado:** validação nativa bloqueia; nenhuma chamada a `/purchases`
- **CA:** CA-required - **Regra:** #5
- **Observacoes:** -

### CT-005 - Select de cartão lista apenas cartões ACTIVE
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** cartões ACTIVE + INACTIVE (via API)
- **Passos:**
  1. Abrir `credit-cards-purchase-card-select`
  2. Listar opções
- **Dados de entrada:** cartões mistos
- **Resultado esperado:** só ACTIVE, com label "nome — R$ disponível"
- **CA:** CA-select-cartao - **Regra:** #2
- **Observacoes:** -

### CT-006 - Select de categoria lista apenas EXPENSE ACTIVE
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** categorias EXPENSE + INCOME + INACTIVE (via API)
- **Passos:**
  1. Abrir `credit-cards-purchase-category-select`
  2. Listar opções
- **Dados de entrada:** categorias mistas
- **Resultado esperado:** só EXPENSE ACTIVE
- **CA:** CA-select-categoria - **Regra:** #2
- **Observacoes:** página carrega `listCategories('EXPENSE')`

### CT-007 - Compra registrada aparece na lista com cartão/categoria/Nx
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** 1 compra criada via form (CT-001)
- **Passos:**
  1. Observar `credit-cards-purchases-list`
  2. Conferir item `credit-cards-purchase-{id}`
- **Dados de entrada:** -
- **Resultado esperado:** item com descrição, `creditCardName · categoryName · Nx` e valor formatado
- **CA:** CA-item-lista - **Regra:** #3
- **Observacoes:** lista = `GET /purchases?page=0&size=10`

### CT-008 - Lista de compras vazia exibe empty state
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário sem compras
- **Passos:**
  1. Abrir `/cartoes`
  2. Aguardar seção
- **Dados de entrada:** -
- **Resultado esperado:** `credit-cards-purchases-empty` visível
- **CA:** CA-compras-vazias - **Regra:** #6
- **Observacoes:** -

### CT-009 - Data futura é aceita no form de compra
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** cartão ACTIVE + categoria EXPENSE
- **Passos:**
  1. Preencher `credit-cards-purchase-date-input` com data futura
  2. Submeter
- **Dados de entrada:** data > hoje
- **Resultado esperado:** compra criada (sem `max`; backend não valida)
- **CA:** CA-data-futura - **Regra:** #8
- **Observacoes:** -

### CT-010 - Resumo used/available permanece inalterado após a compra
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** cartão ACTIVE (limite conhecido)
- **Passos:**
  1. Anotar `credit-cards-summary-used`/`-available`
  2. Registrar compra
  3. Reobservar resumo
- **Dados de entrada:** compra de valor V
- **Resultado esperado:** resumo não muda após registrar a compra
- **CA:** CA-resumo-negativo - **Regra:** #4
- **Observacoes:** [SUPOSICAO] comportamento — conferir no app ao automatizar

### CT-011 - [-] Ver/fechar/pagar fatura pela UI não existe (InvoicesPage órfã)
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** fatura OPEN existente (gerada por compra)
- **Passos:**
  1. Conferir `AppRoutes.tsx` — sem rota para `InvoicesPage`
  2. Conferir ausência de links na UI
- **Dados de entrada:** -
- **Resultado esperado:** `InvoicesPage` nunca é exibida (sem rota); fatura só acessível via API
- **CA:** CA-sem-tela-fatura - **Regra:** #9
- **Observacoes:** adicionado na revisão (gap de cobertura); [ - ] limitação do app — verificação por inspeção

### CT-012 - [-] Editar/excluir compra pela UI não existe
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** compra existente na lista
- **Passos:**
  1. Inspecionar `credit-cards-purchases-list` — ausência de botões de editar/excluir
- **Dados de entrada:** -
- **Resultado esperado:** apenas criação; nenhuma ação de edição/exclusão na lista de compras
- **CA:** CA-sem-edicao-compra - **Regra:** #10
- **Observacoes:** adicionado na revisão (gap de cobertura); [ - ] limitação do app — verificação por inspeção

---