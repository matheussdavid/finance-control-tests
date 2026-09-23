# Casos de Teste UI - Transferências

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/transferencias/transferencias.md

## Resumo Executivo
- **Total de casos de teste:** 11
- **Executados:** 0/11 (0%)
- **Distribuição por prioridade:**
  - P0: 2
  - P1: 5
  - P2: 2
  - P3: 2
- **Critérios de aceite cobertos:** 9 de 9 (UI) após revisão — incluído CA de sem-filtros (CT-011)
- **Regras de negócio cobertas:** 9/9 — regra 3 corrigida (data futura bloqueada por `max`); regra 9 ganhou CT-011
- **Observações gerais:** form rápido e modal compartilham estado/handler únicos.
  Estado via API (`AccountFixture`). Revisão: CT-011 (erro de mesma conta no modal) fundido no CT-003 como
  variação parametrizada; adicionado CT-011 (sem filtros). Feature nova, nenhum teste automatizado.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Transferência válida pelo form rápido cria item na lista | P0 | Positivo | CA-transferencia-ui-feliz | #2 |
| [ ] | CT-002 | Transferência válida pelo modal fecha o modal e cria item | P0 | Positivo | CA-modal-feliz | #2 |
| [ ] | CT-003 | Mesma conta origem/destino exibe erro da API (form rápido e modal) | P1 | Negativo | CA-erro-api-same-account | #4 |
| [ ] | CT-004 | Campos obrigatórios vazios impedem submit e não chamam a API | P1 | Negativo | CA-campos-obrigatorios | #7 |
| [ ] | CT-005 | Lista vazia exibe empty state | P1 | Borda | CA-lista-vazia | #6 |
| [ ] | CT-006 | Selects exibem apenas contas ACTIVE | P1 | Positivo | CA-selects-contas-ativas | #1 |
| [ ] | CT-007 | Data futura é bloqueada no input (max=today) e não chama a API | P1 | Borda | CA-data-futura | #3 |
| [ ] | CT-008 | Paginação: next avança e prev desabilitado na primeira página | P2 | Borda | CA-paginacao | #5 |
| [ ] | CT-009 | Cancelar no modal fecha sem criar transferência | P2 | Positivo | CA-modal-cancel | #4 |
| [-] | CT-010 | [-] Editar/excluir transferência pela UI não existe | P3 | Borda | CA-sem-edicao-ui | #8 |
| [-] | CT-011 | [-] Busca/filtros de transferências não existem na UI | P3 | Borda | CA-sem-filtros-ui | #9 |

---

### CT-001 - Transferência válida pelo form rápido cria item na lista
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** duas contas ACTIVE via API; tela `/transferencias`
- **Passos:**
  1. Preencher `transfers-quick-form-source-select` e `-destination-select` (contas distintas)
  2. Preencher `transfers-quick-form-amount-input` e `-date-input`
  3. Clicar em `transfers-quick-form-submit-btn`
- **Dados de entrada:** contas + amount/date/datenação
- **Resultado esperado:** item `transfers-list-item-{id}` com nomes das contas, valor e data; form é limpo
- **CA:** CA-transferencia-ui-feliz - **Regra:** #2
- **Observacoes:** método esperado: `deveCriarTransferenciaPeloFormRapido`

### CT-002 - Transferência válida pelo modal fecha o modal e cria item
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** duas contas ACTIVE via API; tela `/transferencias`
- **Passos:**
  1. Clicar em `transfers-page-new-btn` (abre `transfers-modal`)
  2. Preencher `transfers-modal-form` (source/destination/amount/date)
  3. Clicar em `transfers-modal-submit-btn`
- **Dados de entrada:** contas + amount/date
- **Resultado esperado:** modal fecha, lista recarrega e novo item aparece
- **CA:** CA-modal-feliz - **Regra:** #2
- **Observacoes:** o modal usa o mesmo `handleSubmit`/estado do form rápido

### CT-003 - Mesma conta origem/destino exibe erro da API (form rápido e modal)
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** 1 conta ACTIVE; tela `/transferencias`
- **Passos:**
  1. Selecionar a mesma conta em origem e destino
  2. Preencher amount/date e submeter — **parametrizado**: form rápido (`transfers-quick-form`) e modal (`transfers-modal-form`)
  3. Verificar erro e ausência de item novo
- **Dados de entrada:** origem == destino (form rápido e modal)
- **Resultado esperado:** `message-error` com `transfer.sameAccount` (pt-BR); no modal também `transfers-modal-error` (modal permanece aberto); nenhum item criado
- **CA:** CA-erro-api-same-account - **Regra:** #4
- **Observacoes:** revisão: cenário do antigo CT-011 (modal) fundido aqui como variação parametrizada

### CT-004 - Campos obrigatórios vazios impedem submit e não chamam a API
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/transferencias`
- **Passos:**
  1. Submeter `transfers-quick-form` sem origem/destino/amount/date
- **Dados de entrada:** form vazio
- **Resultado esperado:** validação nativa (`required`) bloqueia; nenhuma chamada a `/transfers`
- **CA:** CA-campos-obrigatorios - **Regra:** #7
- **Observacoes:** garantir "sem chamada" por construção (form inválido)

### CT-005 - Lista vazia exibe empty state
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário sem transferências
- **Passos:**
  1. Abrir `/transferencias`
  2. Aguardar carregamento
- **Dados de entrada:** -
- **Resultado esperado:** `transfers-list-empty` visível
- **CA:** CA-lista-vazia - **Regra:** #6
- **Observacoes:** -

### CT-006 - Selects exibem apenas contas ACTIVE
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** 1 conta ACTIVE + 1 INACTIVE (via API)
- **Passos:**
  1. Abrir `transfers-quick-form-source-select`
  2. Listar as opções
- **Dados de entrada:** contas com status mistos
- **Resultado esperado:** só a conta ACTIVE aparece nas opções de origem e destino
- **CA:** CA-selects-contas-ativas - **Regra:** #1
- **Observacoes:** -

### CT-007 - Data futura é bloqueada no input (max=today) e não chama a API
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** duas contas ACTIVE; tela `/transferencias`
- **Passos:**
  1. Tentar informar data futura em `transfers-quick-form-date-input` (campo com `max={today()}`)
  2. Submeter
- **Dados de entrada:** data > hoje
- **Resultado esperado:** browser bloqueia data futura (validação `max`); nenhuma chamada a `/transfers`
- **CA:** CA-data-futura - **Regra:** #3
- **Observacoes:** corrigido na revisão: o input usa `max={today()}` (`TransfersPage.tsx:170`, `:368`)

### CT-008 - Paginação: next avança e prev desabilitado na primeira página
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** 21+ transferências (size fixo 20)
- **Passos:**
  1. Verificar `transfers-pagination-prev` desabilitado na página 1
  2. Clicar em `transfers-pagination-next`
  3. Verificar avanço e revisita do primeiro item
- **Dados de entrada:** > 20 transferências via API
- **Resultado esperado:** paginação funciona; `prev` desabilitado em `page 0`
- **CA:** CA-paginacao - **Regra:** #5
- **Observacoes:** com `size=20` fixo no service

### CT-009 - Cancelar no modal fecha sem criar transferência
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** tela `/transferencias`
- **Passos:**
  1. Abrir `transfers-modal`
  2. Preencher o form
  3. Clicar em `transfers-modal-cancel-btn`
- **Dados de entrada:** form preenchido
- **Resultado esperado:** modal fecha; nenhuma transferência criada; lista inalterada
- **CA:** CA-modal-cancel - **Regra:** #4
- **Observacoes:** -

### CT-010 - [-] Editar/excluir transferência pela UI não existe
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** tela `/transferencias` com transferências
- **Passos:**
  1. Inspecionar os itens `transfers-list-item-{id}` (ausência de botões edit/delete)
- **Dados de entrada:** -
- **Resultado esperado:** sem ações de edição/exclusão; só criação + listagem
- **CA:** CA-sem-edicao-ui - **Regra:** #8
- **Observacoes:** [ - ] limitação do app — verificação por inspeção da página

### CT-011 - [-] Busca/filtros de transferências não existem na UI
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** tela `/transferencias`
- **Passos:**
  1. Inspecionar a página — ausência de campo de busca/filtro (só paginação `transfers-pagination-{prev,next}`)
- **Dados de entrada:** -
- **Resultado esperado:** sem busca/filtros; apenas paginação (size 20)
- **CA:** CA-sem-filtros-ui - **Regra:** #9
- **Observacoes:** adicionado na revisão (gap de cobertura); [ - ] limitação do app — verificação por inspeção

---