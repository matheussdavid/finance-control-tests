# Casos de Teste UI - Cartões de Crédito

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/cartoes/cartoes.md

## Resumo Executivo
- **Total de casos de teste:** 16
- **Executados:** 0/16 (0%)
- **Distribuição por prioridade:**
  - P0: 2
  - P1: 8
  - P2: 4
  - P3: 2
- **Critérios de aceite cobertos:** 9 de 9 (UI) após revisão — incluído CA-sem-exclusao-ui (CT-016)
- **Regras de negócio cobertas:** 9/9 — regra 1 corrigida (resumo soma apenas cartões ACTIVE)
- **Observações gerais:** `/cartoes` concentra cartões + compras. Message tem testid
  `message-error`/`message-success`. Revisão: CT-010 corrigido (apenas ACTIVE) e
  adicionado CT-016 (sem exclusão/edição na UI). Feature nova, nenhum teste automatizado.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Criar cartão válido exibe item na lista e mensagem de sucesso | P0 | Positivo | CA-criar-ui-feliz | #2 |
| [ ] | CT-002 | Registrar compra válida atualiza resumo used/available | P0 | Positivo | CA-compra-ui-feliz | #6 |
| [ ] | CT-003 | Editar cartão atualiza nome/limite/dias na lista | P1 | Positivo | CA-editar-ui-feliz | #2 |
| [ ] | CT-004 | Desativar cartão esconde as ações e muda o status | P1 | Positivo | CA-desativar-ui-feliz | #3 |
| [ ] | CT-005 | Compra acima do disponível exibe erro e não cria nada | P1 | Negativo | CA-limite-erro-ui | #5 |
| [ ] | CT-006 | Campos obrigatórios do form de cartão vazios impedem submit | P1 | Negativo | CA-required-cartao | #2 |
| [ ] | CT-007 | Campos obrigatórios do form de compra vazios impedem submit | P1 | Negativo | CA-required-compra | #5 |
| [ ] | CT-008 | Select de compra lista apenas cartões ACTIVE | P1 | Positivo | CA-select-cartao-ativo | #4 |
| [ ] | CT-009 | Select de categoria lista apenas EXPENSE ACTIVE | P1 | Positivo | CA-select-categoria | #4 |
| [ ] | CT-010 | Resumo soma limit/used/available apenas dos cartões ACTIVE | P1 | Positivo | CA-resumo-ativos | #1 |
| [ ] | CT-011 | Preview de valor por parcela atualiza no form de compra | P2 | Borda | CA-preview-parcela | #7 |
| [ ] | CT-012 | Lista de compras vazia exibe empty state | P2 | Borda | CA-compras-vazias | #9 |
| [ ] | CT-013 | Cancelar edição zera form e volta ao modo criação | P2 | Positivo | CA-cancelar-edicao | #2 |
| [ ] | CT-014 | Data futura é aceita no form de compra | P2 | Borda | CA-data-futura-compra | #8 |
| [-] | CT-015 | [-] Re-desativar cartão já inativo não é possível pela UI | P3 | Borda | CA-sem-rerdesativar | #3 |
| [-] | CT-016 | [-] Editar/excluir cartão ou compra pela UI não existe | P3 | Borda | CA-sem-exclusao-ui | #12 |

---

### CT-001 - Criar cartão válido exibe item na lista e mensagem de sucesso
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado em `/cartoes`
- **Passos:**
  1. Preencher `credit-cards-form-name-input`, `-limit-input`, `-closing-input`, `-due-input`
  2. Clicar em `credit-cards-form-submit-btn`
- **Dados de entrada:** faker (name, limit>0, days 1-31)
- **Resultado esperado:** `message-success` (`cards.cardCreated`); item `credit-cards-list-item-{id}` com `usedLimit=0`
- **CA:** CA-criar-ui-feliz - **Regra:** #2
- **Observacoes:** método esperado: `deveCriarCartaoPelaInterface`

### CT-002 - Registrar compra válida atualiza resumo used/available
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** cartão ACTIVE + categoria EXPENSE ACTIVE (via API)
- **Passos:**
  1. Preencher `credit-cards-purchase-form` (card, category, amount, date, installments)
  2. Clicar em `credit-cards-purchase-submit-btn`
- **Dados de entrada:** compra via form
- **Resultado esperado:** `message-success` com nº de parcelas; `credit-cards-summary-used` sobe e `-available` cai; item em `credit-cards-purchase-{id}`
- **CA:** CA-compra-ui-feliz - **Regra:** #6
- **Observacoes:** -

### CT-003 - Editar cartão atualiza nome/limite/dias na lista
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** 1 cartão criado (via API)
- **Passos:**
  1. Clicar em `credit-cards-list-item-{id}-edit-btn`
  2. Alterar `name`/`limit`/days e submeter
- **Dados de entrada:** novos valores
- **Resultado esperado:** `message-success` (`cards.cardUpdated`); item mostra os novos valores
- **CA:** CA-editar-ui-feliz - **Regra:** #2
- **Observacoes:** form entra em modo edição (`editingId`); botão cancelar aparece

### CT-004 - Desativar cartão esconde as ações e muda o status
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** 1 cartão ACTIVE
- **Passos:**
  1. Clicar em `credit-cards-list-item-{id}-deactivate-btn`
  2. Verificar lista após reload
- **Dados de entrada:** -
- **Resultado esperado:** `message-success` (`cards.cardDeactivated`); item esmaecido, badge INACTIVE e **sem** botões edit/deactivate
- **CA:** CA-desativar-ui-feliz - **Regra:** #3
- **Observacoes:** -

### CT-005 - Compra acima do disponível exibe erro e não cria nada
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** cartão ACTIVE com limite baixo + categoria EXPENSE; compra > disponível
- **Passos:**
  1. Preencher `credit-cards-purchase-amount-input` com valor acima do disponível
  2. Submeter
- **Dados de entrada:** totalAmount > availableLimit
- **Resultado esperado:** `message-error` com `purchase.insufficientLimit` (pt-BR); nenhum `credit-cards-purchase-{id}` novo; resumo inalterado
- **CA:** CA-limite-erro-ui - **Regra:** #5
- **Observacoes:** -

### CT-006 - Campos obrigatórios do form de cartão vazios impedem submit
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** `/cartoes`
- **Passos:**
  1. Submeter `credit-cards-form` com campos vazios
- **Dados de entrada:** form vazio
- **Resultado esperado:** validação nativa (`required`/`min`) bloqueia; nenhuma chamada a `/credit-cards`
- **CA:** CA-required-cartao - **Regra:** #2
- **Observacoes:** -

### CT-007 - Campos obrigatórios do form de compra vazios impedem submit
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** `/cartoes`
- **Passos:**
  1. Submeter `credit-cards-purchase-form` sem cartão/categoria/valor/data
- **Dados de entrada:** form vazio
- **Resultado esperado:** validação nativa bloqueia; nenhuma chamada a `/purchases`
- **CA:** CA-required-compra - **Regra:** #5
- **Observacoes:** -

### CT-008 - Select de compra lista apenas cartões ACTIVE
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** 1 cartão ACTIVE + 1 INACTIVE (via API)
- **Passos:**
  1. Abrir `credit-cards-purchase-card-select`
  2. Listar opções
- **Dados de entrada:** cartões mistos
- **Resultado esperado:** só o ACTIVE aparece (label com nome + disponível)
- **CA:** CA-select-cartao-ativo - **Regra:** #4
- **Observacoes:** -

### CT-009 - Select de categoria lista apenas EXPENSE ACTIVE
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** categorias EXPENSE ACTIVE + INCOME + INACTIVE (via API)
- **Passos:**
  1. Abrir `credit-cards-purchase-category-select`
  2. Listar opções
- **Dados de entrada:** categorias mistas
- **Resultado esperado:** só categorias ACTIVE do tipo EXPENSE
- **CA:** CA-select-categoria - **Regra:** #4
- **Observacoes:** página só carrega `listCategories('EXPENSE')`

### CT-010 - Resumo soma limit/used/available apenas dos cartões ACTIVE
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** 1 cartão ACTIVE com compra + 1 cartão INACTIVE
- **Passos:**
  1. Observar `credit-cards-summary-{limit,used,available}`
- **Dados de entrada:** cartões mistos com valores conhecidos
- **Resultado esperado:** resumo considera **apenas cartões ACTIVE** (INACTIVE ignorados no reduce)
- **CA:** CA-resumo-ativos - **Regra:** #1
- **Observacoes:** corrigido na revisão: `creditCards.filter(c => c.status === 'ACTIVE')` antes do reduce (`CreditCardsPage.tsx:76`)

### CT-011 - Preview de valor por parcela atualiza no form de compra
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** `/cartoes`
- **Passos:**
  1. Preencher `credit-cards-purchase-amount-input` (ex.: 100) e `-installments-input` (ex.: 3)
  2. Verificar hint
- **Dados de entrada:** total, parcelas
- **Resultado esperado:** hint mostra valor por parcela (client-side `total/count`)
- **CA:** CA-preview-parcela - **Regra:** #7
- **Observacoes:** -

### CT-012 - Lista de compras vazia exibe empty state
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário sem compras
- **Passos:**
  1. Abrir `/cartoes`
  2. Aguardar `credit-cards-purchases-list`
- **Dados de entrada:** -
- **Resultado esperado:** `credit-cards-purchases-empty` visível
- **CA:** CA-compras-vazias - **Regra:** #9
- **Observacoes:** -

### CT-013 - Cancelar edição zera form e volta ao modo criação
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** cartão em modo edição
- **Passos:**
  1. Clicar em `credit-cards-form-cancel-btn`
  2. Verificar título e form
- **Dados de entrada:** -
- **Resultado esperado:** form volta a "Novo cartão", campos resetados, `editingId=null`
- **CA:** CA-cancelar-edicao - **Regra:** #2
- **Observacoes:** cancelar só renderiza em modo edição

### CT-014 - Data futura é aceita no form de compra
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** cartão ACTIVE + categoria EXPENSE
- **Passos:**
  1. Preencher `credit-cards-purchase-date-input` com data futura
  2. Submeter
- **Dados de entrada:** data > hoje
- **Resultado esperado:** compra criada (não há `max` no campo; backend não valida)
- **CA:** CA-data-futura-compra - **Regra:** #8
- **Observacoes:** -

### CT-015 - [-] Re-desativar cartão já inativo não é possível pela UI
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** 1 cartão INACTIVE
- **Passos:**
  1. Inspecionar item `credit-cards-list-item-{id}` de cartão inativo
- **Dados de entrada:** -
- **Resultado esperado:** botão `-deactivate-btn` ausente para INACTIVE (estado terminal)
- **CA:** CA-sem-rerdesativar - **Regra:** #3
- **Observacoes:** [ - ] limitação do app — impossível reproduzir 409 pela UI (só via API)

---