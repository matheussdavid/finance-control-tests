# Casos de Teste UI - Transações

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/transacoes/transacoes.md

## Resumo Executivo
- **Total de casos de teste:** 13
- **Executados:** 0/13 (0%)
- **Distribuição por prioridade:**
  - P0: 2
  - P1: 5
  - P2: 4
  - P3: 2 (notas `[-]`)
- **Critérios de aceite cobertos:** 8 de 8 (UI)
- **Regras de negócio cobertas:** 5 de 7 (UI) — regras 1 (QuickCapture) a 7;
  regra 4 (filtro TRANSFER bug) tem CT-009 e regra 5 (quickadd bug) tem CT-013 `[-]`
- **Observações gerais:** estado via API (`FinanceFixture`). Revisão: combinado
  "filtro por conta" e "filtro por categoria" num único CT parametrizado (CT-005);
  CT-012 "erro da API no modal" convertido para `[-]` — fluxo raro porque o
  QuickCapture só lista contas/categorias ativas.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Registrar despesa via QuickCapture exibe no feed | P0 | Positivo | CA-ui-expense | #1 |
| [ ] | CT-002 | Registrar receita via QuickCapture exibe no feed | P0 | Positivo | CA-ui-income | #1 |
| [ ] | CT-003 | QuickCapture com campos obrigatorios vazios nao submete | P1 | Negativo | CA-ui-campos | #1 |
| [ ] | CT-004 | Filtro por tipo EXPENSE filtra o feed | P1 | Positivo | CA-ui-filtro | #7 |
| [ ] | CT-005 | Filtros por conta e categoria filtram o feed | P1 | Positivo | CA-ui-filtro | #7 |
| [ ] | CT-006 | Filtro por periodo start/end filtra o feed | P1 | Positivo | CA-ui-filtro | #7 |
| [ ] | CT-007 | Limpar filtros restaura a lista completa | P1 | Positivo | CA-ui-limpar | #7 |
| [ ] | CT-008 | Paginacao prev/next quando ha mais de uma pagina | P2 | Positivo | CA-ui-paginacao | #7 |
| [ ] | CT-009 | Filtro TRANSFER gera erro 400 da API no Message | P2 | Negativo | CA-ui-transfer-bug | #4 |
| [ ] | CT-010 | Campo data do QuickCapture nao permite data futura | P2 | Negativo | CA-ui-data-futura | #3 |
| [ ] | CT-011 | Feed vazio exibe estado vazio | P2 | Borda | CA-ui-vazio | #6 |
| [-] | CT-012 | Erro da API no modal exibido em quickcapture-error | P3 | Normal | - | #6 |
| [-] | CT-013 | Quickadd do /gastos abre o modal sem pre-selecionar o tipo | P3 | Normal | - | #5 |

---

### CT-001 - Registrar despesa via QuickCapture exibe no feed
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado; conta e categoria EXPENSE via `FinanceFixture`; tela `/gastos`
- **Passos:**
  1. Clicar em `spend-page-new-btn` (abre `quickcapture-modal`)
  2. Preencher `quickcapture-amount-input` e `quickcapture-description-input`
  3. Selecionar conta em `quickcapture-account-select` e categoria em `quickcapture-category-select`
  4. Manter data (`quickcapture-date-input`, default hoje) e clicar em `quickcapture-submit-btn`
- **Dados de entrada:** valor/descrição faker únicos; conta/categoria ativas
- **Resultado esperado:** modal fecha; transação aparece em `spend-page-feed-card`; saldo da conta atualizado
- **CA:** CA-ui-expense - **Regra:** #1
- **Observacoes:** método `deveRegistrarDespesaPeloQuickCapture`

### CT-002 - Registrar receita via QuickCapture exibe no feed
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** conta e categoria INCOME via API; tela `/gastos`
- **Passos:**
  1. Abrir o modal e clicar em `quickcapture-tab-income`
  2. Preencher valor/descrição; selecionar conta e categoria INCOME
  3. Clicar em `quickcapture-submit-btn`
- **Dados de entrada:** receita faker única
- **Resultado esperado:** modal fecha; transação INCOME aparece no feed
- **CA:** CA-ui-income - **Regra:** #1
- **Observacoes:** aba income lista apenas categorias INCOME ativas

### CT-003 - QuickCapture com campos obrigatorios vazios nao submete
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/gastos`; modal aberto (aba expense)
- **Passos:**
  1. Clicar em `quickcapture-submit-btn` com valor/conta/categoria vazios
- **Dados de entrada:** campos obrigatórios ausentes
- **Resultado esperado:** validação `required` bloqueia; modal permanece aberto; nenhuma chamada a `POST /transactions`
- **CA:** CA-ui-campos - **Regra:** #1
- **Observacoes:** garantir "sem chamada" por construção (form inválido)

### CT-004 - Filtro por tipo EXPENSE filtra o feed
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** transações INCOME e EXPENSE criadas via API; tela `/gastos`
- **Passos:**
  1. Abrir `spend-page-filters-panel` (`spend-page-filters-toggle`)
  2. Selecionar `EXPENSE` em `spend-page-filter-type`
- **Dados de entrada:** tipo EXPENSE
- **Resultado esperado:** feed mostra apenas despesas
- **CA:** CA-ui-filtro - **Regra:** #7
- **Observacoes:** trocar o filtro zera `page=0` e recarrega

### CT-005 - Filtros por conta e categoria filtram o feed
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** 2 contas, 2 categorias e transações variadas via API
- **Passos:**
  1. Selecionar uma conta em `spend-page-filter-account` (variação 1)
  2. Selecionar uma categoria em `spend-page-filter-category` (variação 2)
- **Dados de entrada:** accountId / categoryId
- **Resultado esperado:** feed mostra apenas transações da conta/categoria selecionada
- **CA:** CA-ui-filtro - **Regra:** #7
- **Observacoes:** combinado na revisão (original tinha CTs separados por conta e categoria); caso parametrizado

### CT-006 - Filtro por periodo start/end filtra o feed
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** transações em datas distintas via API
- **Passos:**
  1. Preencher `spend-page-filter-start` e `spend-page-filter-end` num intervalo
- **Dados de entrada:** intervalo fechado de datas
- **Resultado esperado:** feed mostra apenas transações dentro do intervalo (inclusive bordas)
- **CA:** CA-ui-filtro - **Regra:** #7
- **Observacoes:** input `type=date` envia `yyyy-MM-dd`

### CT-007 - Limpar filtros restaura a lista completa
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** filtros aplicados (feed parcial)
- **Passos:**
  1. Clicar em `spend-page-filters-clear`
- **Dados de entrada:** filtros ativos
- **Resultado esperado:** filtros resetados (`page=0&size=50`); feed volta à lista completa
- **CA:** CA-ui-limpar - **Regra:** #7
- **Observacoes:** botão só aparece com filtros ativos (`hasActiveFilters`)

### CT-008 - Paginacao prev/next quando ha mais de uma pagina
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** mais transações que `size=50` (ou mudar size via URL); feed paginado
- **Passos:**
  1. Verificar `spend-page-pagination-next` habilitado
  2. Clicar em next e depois em `spend-page-pagination-prev`
- **Dados de entrada:** página 1 → 2 → 1
- **Resultado esperado:** conteúdo da página troca; controls reflectem a página atual (`Página x de y`); prev desabilitado na primeira
- **CA:** CA-ui-paginacao - **Regra:** #7
- **Observacoes:** hard de atingir com size=50 — considerar reduzir `size` via query na montagem (preparo de massa)

### CT-009 - Filtro TRANSFER gera erro 400 da API no Message
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/gastos`
- **Passos:**
  1. Abrir o painel de filtros
  2. Selecionar `TRANSFER` (Transferência) em `spend-page-filter-type`
- **Dados de entrada:** type=TRANSFER
- **Resultado esperado:** `Message` exibe erro 400 da API (bug conhecido — a opção não deveria existir); lista não quebra
- **CA:** CA-ui-transfer-bug - **Regra:** #4
- **Observacoes:** documenta bug do select (`SpendPage.tsx:166`); espelho do CT-023 API

### CT-010 - Campo data do QuickCapture nao permite data futura
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/gastos`; modal aberto
- **Passos:**
  1. Tentar informar uma data futura em `quickcapture-date-input`
- **Dados de entrada:** data futura
- **Resultado esperado:** `max={today()}` bloqueia a seleção (input não aceita data futura)
- **CA:** CA-ui-data-futura - **Regra:** #3
- **Observacoes:** limitação do backend (aceita data futura) fica documentada no CT-026 API

### CT-011 - Feed vazio exibe estado vazio
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário sem transações; tela `/gastos`
- **Passos:**
  1. Abrir `/gastos` com usuário recém-registrado (sem lançamentos)
- **Dados de entrada:** nenhuma transação
- **Resultado esperado:** estado vazio ("Nenhum gasto encontrado" / `spend.emptyTitle`) no `spend-page-feed-card`
- **CA:** CA-ui-vazio - **Regra:** #6
- **Observacoes:** massa: usuário novo via `TestUserFixture`

### CT-012 - Erro da API no modal exibido em quickcapture-error
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** UI
- **Pre-condicoes:** (raro) recurso desativado entre render e submit
- **Passos:**
  1. Forçar erro de negócio no submit do QuickCapture (ex.: conta/categoria desativada entre o load e o submit)
  2. Verificar `quickcapture-error`
- **Dados de entrada:** payload rejeitado pela API
- **Resultado esperado:** `quickcapture-error` exibe a mensagem formatada da API; modal permanece aberto
- **CA:** - **Regra:** #6
- **Observacoes:** [-]: não automatizável de forma estável — o QuickCapture só lista contas/categorias **ACTIVE**, então o erro 409 exige corrida (race) entre render e submit; documenta o caminho de erro `formatApiError`

### CT-013 - Quickadd do /gastos abre o modal sem pre-selecionar o tipo
- **Prioridade:** P3
- **Tipo:** Normal
- **Camada:** UI
- **Pre-condicoes:** tela `/gastos`
- **Passos:**
  1. Clicar em `spend-page-quickadd-income-type` (ou `-transfer-type`)
  2. Inspecionar o modal aberto
- **Dados de entrada:** botão de quickadd de receita/transferência
- **Resultado esperado:** modal abre sempre em modo `expense` (`quickcapture-tab-expense` selecionado) — **bug de UX** (não pré-seleciona o tipo)
- **CA:** - **Regra:** #5
- **Observacoes:** [-]: comportamento divergente documentado (`SpendPage.tsx:94-129`, `QuickCapture.tsx:58`); não vira assert nominal — o fluxo correto é clicar na aba dentro do modal (CT-001/CT-002)

---