# Casos de Teste UI - Contas

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/contas/contas.md

## Resumo Executivo
- **Total de casos de teste:** 10
- **Executados:** 0/10 (0%)
- **Distribuição por prioridade:**
  - P0: 1
  - P1: 5
  - P2: 4
- **Critérios de aceite cobertos:** 8 de 8 (UI)
- **Regras de negócio cobertas:** 6 de 6 (UI)
- **Observações gerais:** estado criado via API (`FinanceFixture`). Revisão:
  combinado "saldo inicial em branco" e "saldo inicial negativo" num único CT
  parametrizado (CT-003) — mesma validação `required`/`min=0`.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Criar conta com valores validos exibe na lista | P0 | Positivo | CA-ui-criar | #1 |
| [ ] | CT-002 | Nome em branco exibe validacao e nao chama a API | P1 | Negativo | CA-ui-campos | #1 |
| [ ] | CT-003 | Saldo inicial em branco ou negativo e bloqueado | P1 | Negativo | CA-ui-saldo | #1 |
| [ ] | CT-004 | Criar conta selecionando tipo SAVINGS/CASH | P1 | Positivo | CA-ui-tipos | #1 |
| [ ] | CT-005 | Editar conta nao exibe campo saldo e atualiza nome/tipo | P1 | Positivo | CA-ui-editar | #2 |
| [ ] | CT-006 | Desativar conta a torna inativa e remove botoes de acao | P1 | Positivo | CA-ui-desativar | #4 |
| [ ] | CT-007 | Mensagens de sucesso ao criar/atualizar/desativar | P2 | Positivo | CA-ui-msg | #3 |
| [ ] | CT-008 | Resumo de saldo total soma apenas contas ativas | P2 | Positivo | CA-ui-resumo | #5 |
| [ ] | CT-009 | Lista vazia exibe estado vazio | P2 | Borda | CA-ui-vazio | #5 |
| [ ] | CT-010 | Nome acima de 100 caracteres e limitado pelo maxLength | P2 | Negativo | CA-ui-maxlength | #1 |

---

### CT-001 - Criar conta com valores validos exibe na lista
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado via API (`TestUserFixture`); tela `/contas`
- **Passos:**
  1. Preencher `accounts-form-name-input` com nome único
  2. Selecionar tipo em `accounts-form-type-select` (CHECKING)
  3. Informar saldo em `accounts-form-balance-input`
  4. Clicar em `accounts-form-submit-btn`
- **Dados de entrada:** massa faker (nome, tipo, saldo ≥ 0)
- **Resultado esperado:** `accounts-list` mostra o item `accounts-list-item-{id}` com nome, tipo e saldo; badge ACTIVE
- **CA:** CA-ui-criar - **Regra:** #1
- **Observacoes:** método `deveCriarContaComValoresValidos`

### CT-002 - Nome em branco exibe validacao e nao chama a API
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/contas`; form em modo criação
- **Passos:**
  1. Deixar `accounts-form-name-input` vazio; preencher tipo e saldo
  2. Clicar em `accounts-form-submit-btn`
- **Dados de entrada:** nome vazio
- **Resultado esperado:** validação `required` do input bloqueia; permanece em `/contas`; nenhuma chamada a `POST /accounts`
- **CA:** CA-ui-campos - **Regra:** #1
- **Observacoes:** garantir "sem chamada" por construção (submit de formulário inválido)

### CT-003 - Saldo inicial em branco ou negativo e bloqueado
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/contas`; form em modo criação
- **Passos:**
  1. Preencher nome/tipo válidos; deixar `accounts-form-balance-input` vazio (variação 1)
  2. Preencher `-10` no saldo (variação 2)
  3. Clicar em `accounts-form-submit-btn`
- **Dados de entrada:** saldo vazio ou negativo
- **Resultado esperado:** validação `required`/`min=0` bloqueia o submit; nenhuma chamada a `POST /accounts`
- **CA:** CA-ui-saldo - **Regra:** #1
- **Observacoes:** combinado na revisão (original tinha casos separados para branco e negativo); caso parametrizado

### CT-004 - Criar conta selecionando tipo SAVINGS/CASH
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** tela `/contas`
- **Passos:**
  1. Selecionar `SAVINGS` (Reservas) no `accounts-form-type-select`
  2. Preencher nome/saldo; submeter
  3. Repetir com `CASH` (Dinheiro)
- **Dados de entrada:** tipos SAVINGS e CASH
- **Resultado esperado:** dois itens na lista com os badges/labels corretos (`Reservas`/`Dinheiro`)
- **CA:** CA-ui-tipos - **Regra:** #1
- **Observacoes:** -

### CT-005 - Editar conta nao exibe campo saldo e atualiza nome/tipo
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** conta criada via API (`FinanceFixture`); tela `/contas`
- **Passos:**
  1. Clicar em `accounts-list-item-{id}-edit-btn`
  2. Verificar ausência de `accounts-form-balance-input`
  3. Alterar nome/tipo e clicar em `accounts-form-submit-btn`
- **Dados de entrada:** novo nome/tipo (sem saldo)
- **Resultado esperado:** formulário sem campo de saldo; item atualizado na lista com nome/tipo novos
- **CA:** CA-ui-editar - **Regra:** #2
- **Observacoes:** confirma que `PUT` não envia `initialBalance`

### CT-006 - Desativar conta a torna inativa e remove botoes de acao
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** conta ativa criada via API
- **Passos:**
  1. Clicar em `accounts-list-item-{id}-deactivate-btn`
  2. Aguardar a lista recarregar
- **Dados de entrada:** id da conta ativa
- **Resultado esperado:** item exibe badge INACTIVE; botões `-edit-btn`/`-deactivate-btn` não existem mais no item
- **CA:** CA-ui-desativar - **Regra:** #4
- **Observacoes:** -

### CT-007 - Mensagens de sucesso ao criar/atualizar/desativar
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado; tela `/contas`
- **Passos:**
  1. Criar conta → verificar `Message` com "Conta criada com sucesso."
  2. Editar conta → "Conta atualizada com sucesso."
  3. Desativar conta → "Conta desativada."
- **Dados de entrada:** fluxos de criação/edição/desativação
- **Resultado esperado:** mensagens do `Message` conforme `accounts.created`/`accounts.updated`/`accounts.deactivated`
- **CA:** CA-ui-msg - **Regra:** #3
- **Observacoes:** assert por texto renderizado (i18n pt.ts)

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
- **Observacoes:** front filtra `status === 'ACTIVE'` antes de somar

### CT-009 - Lista vazia exibe estado vazio
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado sem nenhuma conta; tela `/contas`
- **Passos:**
  1. Abrir `/contas` com usuário recém-registrado (sem contas)
- **Dados de entrada:** nenhuma conta
- **Resultado esperado:** `accounts-list-empty` com "Nenhuma conta cadastrada"
- **CA:** CA-ui-vazio - **Regra:** #5
- **Observacoes:** massa: usuário novo via `TestUserFixture` (não cria conta)

### CT-010 - Nome acima de 100 caracteres e limitado pelo maxLength
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/contas`
- **Passos:**
  1. Digitar 120 caracteres em `accounts-form-name-input`
  2. Verificar o valor efetivo do campo
- **Dados de entrada:** 120 caracteres
- **Resultado esperado:** campo limita a 100 caracteres (`maxLength=100`); submit sem chamada extra à API
- **CA:** CA-ui-maxlength - **Regra:** #1
- **Observacoes:** convenção cliente espelha JSR-303 do backend

---