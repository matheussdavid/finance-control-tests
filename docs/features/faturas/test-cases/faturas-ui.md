# Casos de Teste UI - Faturas

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/faturas/faturas.md

## Resumo Executivo
- **Total de casos de teste:** 4
- **Executados:** 0/4 (0%)
- **Distribuição por prioridade:**
  - P0: 0
  - P1: 2
  - P2: 2
- **Critérios de aceite cobertos:** 0 de 0 (UI) — feature sem CA de UI
  automatizável
- **Regras de negócio cobertas:** 0/4 (UI)
- **Observações gerais:** **InvoicesPage não está roteada** (código morto em
  `frontend/src/pages/InvoicesPage.tsx`; `AppRoutes.tsx` não a importa). Todos
  os casos são `[-]` (não executáveis até a rota existir) e documentam os
  fluxos intencionados + `data-testid` existentes. Faturas na UI só aparecem
  indiretamente em dashboard/planejamento (cobertos nas features respetivas).

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [-] | CT-001 | Acessar a tela de faturas pela navegação | P1 | Borda | - | #1 |
| [-] | CT-002 | Listar faturas e fechar uma fatura aberta | P1 | Positivo | - | #1,#2,#3,#4 |
| [-] | CT-003 | Abrir detalhe da fatura e visualizar parcelas | P2 | Positivo | - | #1,#2,#3,#4 |
| [-] | CT-004 | Pagar fatura fechada pelo modal de detalhe | P2 | Positivo | - | #1,#2,#3,#4 |

---

### CT-001 - Acessar a tela de faturas pela navegação
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** usuário autenticado via API (`TestUserFixture`)
- **Passos:**
  1. Tentar acessar `/faturas` (ou clicar em link de faturas na nav)
  2. Verificar a rota
- **Dados de entrada:** - 
- **Resultado esperado:** [NA] — a rota não existe; `InvoicesPage` não está em
  `AppRoutes`; usuário cai em `*` → redirect para `/`
- **CA:** - **Regra:** #1
- **Observacoes:** [NA] tela inacessível (código morto). Quando a rota for
  adicionada, testar `invoices-page` visível em `/faturas`

### CT-002 - Listar faturas e fechar uma fatura aberta
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com fatura `OPEN` criada via API (`PurchaseFixture`)
- **Passos:**
  1. Acessar a tela de faturas
  2. Localizar `invoices-list-item-{id}` da fatura
  3. Clicar em `invoices-list-item-{id}-close-btn`
- **Dados de entrada:** fatura via API (compra parcelada)
- **Resultado esperado:** [NA] — tela inacessível; fluxo equivalente coberto em
  `FaturaApiTest` (CT-009/CT-012)
- **CA:** - **Regra:** #1,#2,#3,#4
- **Observacoes:** [NA] não executável — `InvoicesPage` sem rota

### CT-003 - Abrir detalhe da fatura e visualizar parcelas
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com fatura com parcelas (via API)
- **Passos:**
  1. Acessar a tela de faturas
  2. Abrir `invoices-detail-modal` (clicar no item)
  3. Verificar `invoices-detail-installment-{id}` e totais
- **Resultado esperado:** [NA] — tela inacessível; `invoices-detail-modal` e
  `invoices-detail-installments` existem no código morto
- **CA:** - **Regra:** #1,#2,#3,#4
- **Observacoes:** [NA] não executável — `InvoicesPage` sem rota

### CT-004 - Pagar fatura fechada pelo modal de detalhe
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário com fatura `CLOSED` e conta `ACTIVE` (via API)
- **Passos:**
  1. Acessar a tela de faturas
  2. Abrir `invoices-detail-modal`
  3. Selecionar conta em `invoices-detail-pay-account-select`
  4. Clicar em `invoices-detail-pay-btn`
- **Resultado esperado:** [NA] — tela inacessível; equivale ao `FaturaApiTest`
  CT-009 (ciclo completo pago)
- **CA:** - **Regra:** #1,#2,#3,#4
- **Observacoes:** [NA] não executável — `InvoicesPage` sem rota. testids
  existem: `invoices-detail-pay-account-select`, `invoices-detail-pay-btn`

---