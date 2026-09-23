# Casos de Teste UI - Categorias

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/categorias/categorias.md

## Resumo Executivo
- **Total de casos de teste:** 10
- **Executados:** 0/10 (0%)
- **Distribuição por prioridade:**
  - P0: 1
  - P1: 5
  - P2: 3
  - P3: 1
- **Critérios de aceite cobertos:** 7 de 7 (UI)
- **Regras de negócio cobertas:** 5 de 6 (UI) — regra 6 (nomes duplicados) extraída da API (CT-025 API) e com CT-010 UI
- **Observações gerais:** estado via API (`FinanceFixture`/`CategoryClient`).
  Revisão: nenhum CT removido/combinado — cobertura fechada na geração.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Criar categoria de despesa aparece na lista | P0 | Positivo | CA-ui-criar | #1 |
| [ ] | CT-002 | Nome em branco exibe validacao e nao chama a API | P1 | Negativo | CA-ui-campos | #1 |
| [ ] | CT-003 | Criar categoria de receita selecionando tipo INCOME | P1 | Positivo | CA-ui-tipos | #2 |
| [ ] | CT-004 | Filtro por tipo lista apenas categorias do tipo | P1 | Positivo | CA-ui-filtro | #3 |
| [ ] | CT-005 | Editar categoria atualiza nome/tipo | P1 | Positivo | CA-ui-editar | #2 |
| [ ] | CT-006 | Desativar categoria marca inativa e esconde acoes | P1 | Positivo | CA-ui-desativar | #5 |
| [ ] | CT-007 | Mensagens de sucesso ao criar/atualizar/desativar | P2 | Positivo | CA-ui-msg | #4 |
| [ ] | CT-008 | Nome acima de 100 caracteres e limitado pelo maxLength | P2 | Negativo | CA-ui-maxlength | #1 |
| [ ] | CT-009 | Lista vazia exibe estado vazio | P2 | Borda | CA-ui-vazio | #4 |
| [ ] | CT-010 | Criar duas categorias com o mesmo nome e permitido | P3 | Borda | CA-ui-conflito-nome | #6 |

---

### CT-001 - Criar categoria de despesa aparece na lista
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado via API (`TestUserFixture`); tela `/categorias`
- **Passos:**
  1. Preencher `categories-form-name-input` com nome único
  2. Manter tipo EXPENSE em `categories-form-type-select` (default)
  3. Clicar em `categories-form-submit-btn`
- **Dados de entrada:** nome faker único; tipo EXPENSE
- **Resultado esperado:** `categories-list` mostra `categories-list-item-{id}` com nome, badge Despesa e status ACTIVE
- **CA:** CA-ui-criar - **Regra:** #1
- **Observacoes:** método `deveCriarCategoriaDeDespesa`

### CT-002 - Nome em branco exibe validacao e nao chama a API
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/categorias`; form em modo criação
- **Passos:**
  1. Deixar `categories-form-name-input` vazio; manter tipo default
  2. Clicar em `categories-form-submit-btn`
- **Dados de entrada:** nome vazio
- **Resultado esperado:** validação `required` bloqueia; permanece em `/categorias`; nenhuma chamada a `POST /categories`
- **CA:** CA-ui-campos - **Regra:** #1
- **Observacoes:** garantir "sem chamada" por construção (submit de formulário inválido)

### CT-003 - Criar categoria de receita selecionando tipo INCOME
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** tela `/categorias`
- **Passos:**
  1. Selecionar `INCOME` (Receita) em `categories-form-type-select`
  2. Preencher nome e submeter
- **Dados de entrada:** tipo INCOME
- **Resultado esperado:** item na lista com badge Receita; `GET /categories` (via API) confirma `type=INCOME`
- **CA:** CA-ui-tipos - **Regra:** #2
- **Observacoes:** default do form é EXPENSE — o teste garante a troca de tipo

### CT-004 - Filtro por tipo lista apenas categorias do tipo
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** categorias INCOME e EXPENSE criadas via API
- **Passos:**
  1. Selecionar `EXPENSE` em `categories-list-filter`
  2. Verificar a lista
  3. Alternar para `INCOME` e repetir
- **Dados de entrada:** filtro INCOME / EXPENSE
- **Resultado esperado:** apenas categorias do tipo selecionado são exibidas a cada seleção
- **CA:** CA-ui-filtro - **Regra:** #3
- **Observacoes:** alterar o filtro dispara `GET /categories?type=...` (recarregamento da lista)

### CT-005 - Editar categoria atualiza nome/tipo
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** categoria criada via API; tela `/categorias`
- **Passos:**
  1. Clicar em `categories-list-item-{id}-edit-btn`
  2. Alterar nome (e tipo) no formulário
  3. Clicar em `categories-form-submit-btn`
- **Dados de entrada:** novo nome/tipo
- **Resultado esperado:** item atualizado na lista com os novos valores
- **CA:** CA-ui-editar - **Regra:** #2
- **Observacoes:** -

### CT-006 - Desativar categoria marca inativa e esconde acoes
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** categoria ativa criada via API
- **Passos:**
  1. Clicar em `categories-list-item-{id}-deactivate-btn`
  2. Aguardar a lista recarregar
- **Dados de entrada:** id da categoria ativa
- **Resultado esperado:** item exibe badge INACTIVE; botões `-edit-btn`/`-deactivate-btn` não existem mais no item
- **CA:** CA-ui-desativar - **Regra:** #5
- **Observacoes:** -

### CT-007 - Mensagens de sucesso ao criar/atualizar/desativar
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado; tela `/categorias`
- **Passos:**
  1. Criar categoria → verificar `Message` com "Categoria criada com sucesso."
  2. Editar categoria → "Categoria atualizada com sucesso."
  3. Desativar categoria → "Categoria desativada."
- **Dados de entrada:** fluxos de criação/edição/desativação
- **Resultado esperado:** mensagens do `Message` conforme `categories.created`/`categories.updated`/`categories.deactivated`
- **CA:** CA-ui-msg - **Regra:** #4
- **Observacoes:** assert por texto renderizado (i18n pt.ts)

### CT-008 - Nome acima de 100 caracteres e limitado pelo maxLength
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/categorias`
- **Passos:**
  1. Digitar 120 caracteres em `categories-form-name-input`
  2. Verificar o valor efetivo do campo
- **Dados de entrada:** 120 caracteres
- **Resultado esperado:** campo limita a 100 caracteres (`maxLength=100`); submit sem chamada extra à API
- **CA:** CA-ui-maxlength - **Regra:** #1
- **Observacoes:** convenção cliente espelha JSR-303 do backend

### CT-009 - Lista vazia exibe estado vazio
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário recém-registrado (sem categorias); tela `/categorias`
- **Passos:**
  1. Abrir `/categorias` com usuário sem nenhuma categoria
- **Dados de entrada:** nenhuma categoria
- **Resultado esperado:** `categories-list-empty` com "Nenhuma categoria encontrada"
- **CA:** CA-ui-vazio - **Regra:** #4
- **Observacoes:** massa: usuário novo via `TestUserFixture` (não cria categoria)

### CT-010 - Criar duas categorias com o mesmo nome e permitido
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** tela `/categorias`
- **Passos:**
  1. Criar categoria com nome N (EXPENSE)
  2. Criar segunda categoria com o mesmo nome N (INCOME)
- **Dados de entrada:** mesmo `name` em duas criações
- **Resultado esperado:** as duas aparecem na lista (sem erro de conflito — UI não bloqueia nomes duplicados)
- **CA:** CA-ui-conflito-nome - **Regra:** #6
- **Observacoes:** eco da limitação de API (sem unicidade de nome)

---