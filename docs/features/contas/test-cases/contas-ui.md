# Casos de Teste UI - Contas

**Data de geração:** 2026-09-24
**Autor:** QA Agent
**Fonte:** docs/features/contas/contas.md
**Última atualização:** 2026-09-24 (agrupado por página; mensagens fundidas nos happy paths)

## Resumo Executivo
- **Total de casos de teste:** 10
- **Executados:** 0/10 (0%)
- **Distribuição por prioridade:**
  - P0: 1
  - P1: 5
  - P2: 4
- **Critérios de aceite cobertos:** 10 de 10 (UI) — inclui `CA-ui-erro-api` (novo, regra 6)
- **Regras de negócio cobertas:** 6 de 6 (UI) — regra 6 (erro de API no `Message`) ganhou CT próprio
- **Observações gerais:** CTs agrupados por `AccountsPage` em modos: criação, edição, desativação e resumo/borda. Mensagens de sucesso (`accounts.created`/`accounts.updated`/`accounts.deactivated`) fundidas como passo dos happy paths (CA #3), eliminando o CT transversal anterior. BVA na UI: campos com `maxLength` (name 100) bloqueiam >max no browser — não há como submeter 101 pela UI (nota em CT-010); saldo `min=0`/`required` bloqueiam vazio e negativo (CT-003 — BVA: min-1=-0.01, min=0.00). Estado criado via API (`TestUserFixture`/fixtures de conta). CT-007 (erro de API) força 409 desativando a conta em segundo plano via API antes do clique.

---

## Casos de Teste

### AccountsPage (criação)

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Criar conta com valores validos exibe na lista e mensagem de sucesso | P0 | Positivo | CA-ui-criar, CA-ui-msg | #1, #3 |
| [ ] | CT-002 | Nome em branco exibe validacao e nao chama a API | P1 | Negativo | CA-ui-campos | #1 |
| [ ] | CT-003 | Saldo inicial em branco ou negativo e bloqueado | P1 | Negativo | CA-ui-saldo | #1 |
| [ ] | CT-004 | Criar conta selecionando tipo SAVINGS/CASH | P1 | Positivo | CA-ui-tipos | #1 |

### AccountsPage (edição)

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-005 | Editar conta nao exibe campo saldo, atualiza nome/tipo e mostra mensagem | P1 | Positivo | CA-ui-editar, CA-ui-msg | #2, #3 |

### AccountsPage (desativação)

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-006 | Desativar conta a torna inativa, remove botoes de acao e mostra mensagem | P1 | Positivo | CA-ui-desativar, CA-ui-msg | #4, #3 |
| [ ] | CT-007 | Erro de API ao desativar conta e exibido no Message | P2 | Negativo | CA-ui-erro-api | #6 |

### AccountsPage (resumo e borda)

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-008 | Resumo de saldo total soma apenas contas ativas | P2 | Positivo | CA-ui-resumo | #5 |
| [ ] | CT-009 | Lista vazia exibe estado vazio | P2 | Borda | CA-ui-vazio | #5 |
| [ ] | CT-010 | Nome acima de 100 caracteres e limitado pelo maxLength | P2 | Borda | CA-ui-maxlength | #1 |

---

## Detalhamento por página

### AccountsPage (criação)

### CT-001 - Criar conta com valores validos exibe na lista e mensagem de sucesso
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado via API (`TestUserFixture`); tela `/contas`
- **Passos:**
  1. Preencher `accounts-form-name-input` com nome único
  2. Selecionar tipo em `accounts-form-type-select` (CHECKING)
  3. Informar saldo em `accounts-form-balance-input`
  4. Clicar em `accounts-form-submit-btn`
  5. Asserir a `Message` de sucesso e o item na lista
- **Dados de entrada:** massa faker (nome único, tipo, saldo ≥ 0)
- **Resultado esperado:** `accounts-list` mostra o item `accounts-list-item-{id}` com nome, tipo e saldo; badge ACTIVE; `Message` "Conta criada com sucesso." (`accounts.created`)
- **CA:** CA-ui-criar, CA-ui-msg - **Regra:** #1, #3
- **Observacoes:** regra 3 fundida aqui (assert da mensagem dentro do happy path); sucesso recarrega a lista

### CT-002 - Nome em branco exibe validacao e nao chama a API
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/contas`; form em modo criação
- **Passos:**
  1. Deixar `accounts-form-name-input` vazio; preencher tipo e saldo
  2. Clicar em `accounts-form-submit-btn`
- **Dados de entrada:** nome vazio (BVA: min-1 — campo obrigatório)
- **Resultado esperado:** validação `required` do input bloqueia; permanece em `/contas`; nenhuma chamada a `POST /accounts`
- **CA:** CA-ui-campos - **Regra:** #1
- **Observacoes:** garantir "sem chamada" por construção (submit de formulário inválido não dispara `onSubmit`)

### CT-003 - Saldo inicial em branco ou negativo e bloqueado
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/contas`; form em modo criação
- **Passos:**
  1. Preencher nome/tipo válidos; deixar `accounts-form-balance-input` vazio (variação 1)
  2. Preencher `-0.01` no saldo (variação 2 — BVA min-1)
  3. Preencher `-10` no saldo (variação 3)
  4. Clicar em `accounts-form-submit-btn`
- **Dados de entrada:** saldo vazio, -0.01 e -10 (BVA: min-1=-0.01, min=0.00)
- **Resultado esperado:** validação `required`/`min=0` do input bloqueia o submit; nenhuma chamada a `POST /accounts`
- **CA:** CA-ui-saldo - **Regra:** #1
- **Observacoes:** caso parametrizado (3 variações); limite `min=0`/`step=0.01` conferido no `AccountsPage.tsx`; browser bloqueia antes da API

### CT-004 - Criar conta selecionando tipo SAVINGS/CASH
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** tela `/contas`
- **Passos:**
  1. Selecionar `SAVINGS` (Reservas) no `accounts-form-type-select`
  2. Preencher nome/saldo; submeter
  3. Repetir com `CASH` (Dinheiro)
- **Dados de entrada:** tipos SAVINGS e CASH (nomes únicos por execução)
- **Resultado esperado:** itens na lista com os labels corretos (`Reservas`/`Dinheiro`) e badge ACTIVE
- **CA:** CA-ui-tipos - **Regra:** #1
- **Observacoes:** CHECKING já coberto em CT-001

### AccountsPage (edição)

### CT-005 - Editar conta nao exibe campo saldo, atualiza nome/tipo e mostra mensagem
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** conta criada via API; tela `/contas`
- **Passos:**
  1. Clicar em `accounts-list-item-{id}-edit-btn`
  2. Verificar ausência de `accounts-form-balance-input`
  3. Alterar nome/tipo e clicar em `accounts-form-submit-btn`
  4. Asserir a `Message` e o item atualizado na lista
- **Dados de entrada:** novo nome/tipo únicos (sem saldo)
- **Resultado esperado:** formulário sem campo de saldo; item atualizado com nome/tipo novos; `Message` "Conta atualizada com sucesso." (`accounts.updated`)
- **CA:** CA-ui-editar, CA-ui-msg - **Regra:** #2, #3
- **Observacoes:** confirma que `PUT` não envia `initialBalance` (campo oculto em modo edição — `{!editingId && ...}`); regra 3 fundida

### AccountsPage (desativação)

### CT-006 - Desativar conta a torna inativa, remove botoes de acao e mostra mensagem
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** conta ativa criada via API
- **Passos:**
  1. Clicar em `accounts-list-item-{id}-deactivate-btn`
  2. Aguardar a lista recarregar
  3. Asserir a `Message` e o estado do item
- **Dados de entrada:** id da conta ativa
- **Resultado esperado:** `Message` "Conta desativada." (`accounts.deactivated`); item exibe badge INACTIVE; botões `-edit-btn`/`-deactivate-btn` não existem mais no item
- **CA:** CA-ui-desativar, CA-ui-msg - **Regra:** #4, #3
- **Observacoes:** regras 3 e 4 fundidas; desativação também remove o item da soma do resumo (CT-008 cobre a soma)

### CT-007 - Erro de API ao desativar conta e exibido no Message
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** conta ativa criada via API; tela `/contas` aberta
- **Passos:**
  1. Criar a conta via API e abrir `/contas` (item ACTIVE com botão `-deactivate-btn`)
  2. Desativar a conta em segundo plano via API (`PATCH /accounts/{id}/deactivate`)
  3. Clicar no `accounts-list-item-{id}-deactivate-btn` (botão ainda renderizado)
  4. Asserir o `Message` de erro
- **Dados de entrada:** id da conta desativada por fora da UI
- **Resultado esperado:** `Message` exibindo o erro formatado por `formatApiError` (409 — `account.alreadyInactive`); permanece em `/contas`, sem navegação
- **CA:** CA-ui-erro-api - **Regra:** #6
- **Observacoes:** regra 6; o único caminho de erro de API na UI é corrida (estado divergente via API); verify-state: assegurar que o botão ficou stale após a desativação externa antes de clicar

### AccountsPage (resumo e borda)

### CT-008 - Resumo de saldo total soma apenas contas ativas
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** 2 contas ativas (via API) e 1 desativada
- **Passos:**
  1. Abrir `/contas`
  2. Ler valor em `accounts-summary-card`
- **Dados de entrada:** saldos ativos 100 + 200; inativa 500
- **Resultado esperado:** total exibido = 300 (`accounts.totalActiveBalance`); nome/contexto do card presente
- **CA:** CA-ui-resumo - **Regra:** #5
- **Observacoes:** front filtra `status === 'ACTIVE'` antes de somar (`activeAccounts.filter`)

### CT-009 - Lista vazia exibe estado vazio
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado sem nenhuma conta; tela `/contas`
- **Passos:**
  1. Abrir `/contas` com usuário recém-registrado (sem contas)
- **Dados de entrada:** nenhuma conta
- **Resultado esperado:** `accounts-list-empty` visível ("Nenhuma conta cadastrada")
- **CA:** CA-ui-vazio - **Regra:** #5
- **Observacoes:** massa: usuário novo via `TestUserFixture` (não cria conta); garante isolamento entre CTs

### CT-010 - Nome acima de 100 caracteres e limitado pelo maxLength
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** tela `/contas`
- **Passos:**
  1. Digitar 120 caracteres em `accounts-form-name-input`
  2. Verificar o valor efetivo do campo
- **Dados de entrada:** 120 caracteres (BVA: max+1 — ultrapassa o limite)
- **Resultado esperado:** campo limita a 100 caracteres (`maxLength=100`); submit envia valor truncado válido
- **CA:** CA-ui-maxlength - **Regra:** #1
- **Observacoes:** convenção cliente espelha `@Size(max=100)` do backend; **não há como submeter 101 pela UI** (browser bloqueia) — decisão BVA registrada; o BVA de `max+1` fica na camada API (CT-006)

---