# Casos de Teste UI - Orçamentos e Reservas

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/orcamentos-reservas/orcamentos-reservas.md

## Resumo Executivo
- **Total de casos de teste:** 9
- **Executados:** 0/9 (0%)
- **Distribuição por prioridade:**
  - P0: 2
  - P1: 4
  - P2: 3
- **Critérios de aceite cobertos:** 4 de 4 (UI)
- **Regras de negócio cobertas:** 5/6 (UI) — regra 6 (botão morto / sem
  edição-remoção) coberta por CT-008/CT-009 `[-]`
- **Observações gerais:** UI de orçamentos é a rota **/reservas**
  (`SavingsPage`, rótulo "reserva"). `BudgetsPage.tsx` é órfã (sem rota).
Estado de gasto pré-condição criado por API (transação/compra) e a UI apenas
reflete; para testar `spent`/estouro, pré-popular via API antes de abrir a
tela. Dois casos são `[-]` (CT-008/CT-009) por limitação de produto/UI.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Tela /reservas exibe resumo e lista de reservas existentes | P0 | Positivo | CA-exibir-resumo | #1,#3 |
| [ ] | CT-002 | Criar reserva pelo modal e vê-la na lista | P0 | Positivo | CA-criar-reserva | #2,#3 |
| [ ] | CT-003 | Select do modal lista apenas categorias EXPENSE ativas | P1 | Borda | CA-modal-categorias | #2 |
| [ ] | CT-004 | Reserva estourada exibe badge de atenção e progresso vermelho | P1 | Negativo | CA-estouro | #4 |
| [ ] | CT-005 | Erro duplicado (409) aparece no modal e não fecha | P1 | Negativo | CA-erro-api | #5 |
| [ ] | CT-006 | Resumo atualiza após criar nova reserva | P1 | Positivo | CA-exibir-resumo | #3 |
| [ ] | CT-007 | Botão cancelar fecha o modal sem salvar | P2 | Borda | - | #2 |
| [-] | CT-008 | Editar ou remover reserva pela UI não existe | P2 | Limitação | - | #6 |
| [-] | CT-009 | Botão ver planejamento completo sem ação | P2 | Borda | - | #6 |

---

### CT-001 - Tela /reservas exibe resumo e lista de reservas existentes
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com duas reservas via API (`BudgetFixture`),
  valores R$ 500 e R$ 300; autenticado
- **Passos:**
  1. Navegar para `/reservas`
  2. Verificar `savings-summary-budgeted` = R$ 800, `savings-summary-spent` e
     `savings-summary-available` conforme massa
  3. Verificar `savings-list-item-{id}` dos dois itens
- **Dados de entrada:** reservas via `BudgetFixture`
- **Resultado esperado:** resumo e lista refletem os orçamentos criados
- **CA:** CA-exibir-resumo - **Regra:** #1,#3
- **Observacoes:** coberto por `SavingsWebTest#deveExibirResumoDeReservas`; massa
  via API antes de abrir a tela

### CT-002 - Criar reserva pelo modal e vê-la na lista
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com categoria EXPENSE; tela `/reservas`
- **Passos:**
  1. Clicar em `savings-page-new-btn`
  2. Preencher `savings-modal-category-select`, `savings-modal-amount-input`,
     `savings-modal-month-select`, `savings-modal-year-select`
  3. Clicar em `savings-modal-submit-btn`
  4. Verificar item na lista e resumo atualizado
- **Dados de entrada:** categoria EXPENSE + valor faker + mês/ano
- **Resultado esperado:** modal fecha; item aparece em `savings-list-item-{id}`;
  resumo soma novo valor
- **CA:** CA-criar-reserva - **Regra:** #2,#3
- **Observacoes:** coberto por `SavingsWebTest#deveCriarReservaViaModal`

### CT-003 - Select do modal lista apenas categorias EXPENSE ativas
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário com categorias INCOME e EXPENSE (e uma EXPENSE
  inativa via API)
- **Passos:**
  1. Abrir `savings-modal`
  2. Listar as opções de `savings-modal-category-select`
- **Dados de entrada:** categorias via `CategoryFixture`
- **Resultado esperado:** apenas categorias EXPENSE ativas no select
- **CA:** CA-modal-categorias - **Regra:** #2
- **Observacoes:** [SUPOSICAO] filtro client `c.type === 'EXPENSE' &&
  c.status === 'ACTIVE'`; validar ao implementar

### CT-004 - Reserva estourada exibe badge de atenção e progresso vermelho
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** usuário com reserva de R$ 200 na categoria e gasto de
  R$ 300 (transação EXPENSE via API no mês)
- **Passos:**
  1. Navegar para `/reservas`
  2. Verificar item com `available` negativo
  3. Verificar badge "Atenção" e progresso vermelho
- **Dados de entrada:** orçamento via API + transação EXPENSE na categoria
- **Resultado esperado:** badge de atenção presente; percentual > 100% com cor
  de despesa
- **CA:** CA-estouro - **Regra:** #4
- **Observacoes:** estado de gasto via API (transação) antes de abrir a tela

### CT-005 - Erro duplicado (409) aparece no modal e não fecha
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** usuário com reserva existente (categoria, mês, ano)
- **Passos:**
  1. Abrir `savings-modal`
  2. Criar reserva com a mesma combinação (categoria, mês, ano)
  3. Clicar em `savings-modal-submit-btn`
- **Dados de entrada:** combinação duplicada da reserva existente
- **Resultado esperado:** `savings-modal-error` com mensagem do 409
  (`budget.alreadyExists`); modal permanece aberto
- **CA:** CA-erro-api - **Regra:** #5
- **Observacoes:** coberto por `SavingsWebTest#deveExibirErroDeReservaDuplicada`

### CT-006 - Resumo atualiza após criar nova reserva
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com 1 reserva existente; tela `/reservas`
- **Passos:**
  1. Criar segunda reserva via modal
  2. Verificar `savings-summary-budgeted` com o total somado
- **Dados de entrada:** nova reserva com valor faker
- **Resultado esperado:** `savings-summary-budgeted` soma as duas reservas
- **CA:** CA-exibir-resumo - **Regra:** #3
- **Observacoes:** deriva de re-listagem após submit (CT-002 cobre parte)

### CT-007 - Botão cancelar fecha o modal sem salvar
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** tela `/reservas`
- **Passos:**
  1. Abrir `savings-modal`
  2. Preencher campos
  3. Clicar em `savings-modal-cancel-btn`
- **Dados de entrada:** campos preenchidos sem submeter
- **Resultado esperado:** modal fecha; nenhuma chamada de criação; lista inalterada
- **CA:** - **Regra:** #2
- **Observacoes:** valida ausência de POST ao cancelar

### CT-008 - Editar ou remover reserva pela UI não existe
- **Prioridade:** P2
- **Tipo:** Limitação
- **Camada:** UI
- **Pre-condicoes:** tela `/reservas`
- **Passos:**
  1. [NA] procurar ação de editar/remover na lista ou modal
- **Dados de entrada:** - 
- **Resultado esperado:** [NA] `SavingsPage` não oferece edição/remoção na UI
  (só criação e listagem); PUT é só-API e DELETE não existe
- **CA:** - **Regra:** #6
- **Observacoes:** [-] limitação da UI; edição via API é coberta por
  `BudgetApiTest` (CT-010/CT-016)

### CT-009 - Botão ver planejamento completo sem ação
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** tela `/reservas` com reservas ativas
- **Passos:**
  1. Localizar `savings-list-view-planning`
  2. Clicar no botão
- **Dados de entrada:** - 
- **Resultado esperado:** [NA] botão sem `onClick` (`SavingsPage.tsx:138-145`) —
  nenhuma navegação/efeito
- **CA:** - **Regra:** #6
- **Observacoes:** [-] botão morto; não testar clique com ação esperada (bug de
  produto documentado)

---