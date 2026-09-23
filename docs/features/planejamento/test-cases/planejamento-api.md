# Casos de Teste API - Planejamento

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/planejamento/planejamento.md

## Resumo Executivo
- **Total de casos de teste:** 3
- **Executados:** 0/3 (0%)
- **Distribuição por prioridade:**
  - P0: 0
  - P1: 0
  - P2: 3
- **Critérios de aceite cobertos:** N/A (feature sem CA de API próprio)
- **Regras de negócio cobertas:** 0/1 (API) — regra única "sem endpoint próprio"
  registrada como nota
- **Observações gerais:** **não existe endpoint `/planning`** — o planejamento é
  uma view composta do frontend sobre `/dashboard`, `/budgets` e `/invoices`.
  A API já está coberta nas features `faturas`, `orcamentos-reservas` e
  `dashboard`. Este arquivo traz apenas casos `[-]` de referência/rastreamento —
  **nenhum teste novo de API deve ser criado para esta feature**. Casos de
  integração visuais e de composição vivem em `planejamento-ui.md` (fase E2E
  declarada).

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [-] | CT-001 | Endpoint próprio de planejamento | P2 | Referência | - | #1 |
| [-] | CT-002 | Composição de API gerando métricas do planejamento | P2 | Referência | - | #1 |
| [-] | CT-003 | Contrato de response de planejamento | P2 | Referência | - | #1 |

---

### CT-001 - Endpoint próprio de planejamento
- **Prioridade:** P2
- **Tipo:** Referência
- **Camada:** API
- **Pre-condicoes:** - 
- **Passos:**
  1. [NA] verificar existência de `GET /planning` no `DashboardController`
- **Dados de entrada:** - 
- **Resultado esperado:** [NA] não existe — o `/planejamento` do frontend usa
  `/dashboard`, `/budgets` e `/invoices`
- **CA:** - **Regra:** #1
- **Observacoes:** [-] não implementar teste; a API de composição é coberta nas
  features `faturas` (faturas-api.md), `orcamentos-reservas`
  (orcamentos-reservas-api.md) e `dashboard` (dashboard-api.md)

### CT-002 - Composição de API gerando métricas do planejamento
- **Prioridade:** P2
- **Tipo:** Referência
- **Camada:** API
- **Pre-condicoes:** usuário com transações, faturas e orçamentos (via fixtures)
- **Passos:**
  1. [NA] chamar `/dashboard?month=`, `/budgets`, `/invoices` isoladamente
  2. [NA] somar os resultados como o front (overview + projeção)
- **Dados de entrada:** - 
- **Resultado esperado:** [NA] métricas derivadas já assertadas nos CTs de
  origem (dashboard income/expenses, budgets spent, invoices OPEN)
- **CA:** - **Regra:** #1
- **Observacoes:** [-] referência; a asserção de composição é feita na UI
  (planejamento-ui.md, fase E2E)

### CT-003 - Contrato de response de planejamento
- **Prioridade:** P2
- **Tipo:** Referência
- **Camada:** API
- **Schema:** - (nenhum — sem endpoint)
- **Pre-condicoes:** - 
- **Passos:**
  1. [NA] validar schema de response de `/planning`
- **Dados de entrada:** - 
- **Resultado esperado:** [NA] schema vazio/inexistente — sem endpoint, sem
  resposta própria; os shapes são `DashboardResponse`, `BudgetResponse[]` e
  `Page<InvoiceResponse>` das features de origem
- **CA:** - **Regra:** #1
- **Observacoes:** [-] não criar schema `planejamento/*`; reutilizar
  `schemas/dashboard/*`, `schemas/orcamentos/*`, `schemas/faturas/*`

---