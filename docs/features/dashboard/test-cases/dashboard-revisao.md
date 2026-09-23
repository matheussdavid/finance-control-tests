# Relatório de Revisão - Dashboard

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/dashboard-api.md`, `test-cases/dashboard-ui.md`

## Resumo da revisão
- **CTs originais (API):** 17
- **CTs originais (UI):** 13
- **CTs removidos (duplicados):** 0
- **CTs combinados:** 5 (2 na API + 3 na UI)
- **CTs finais (API):** 15
- **CTs finais (UI):** 10

## CTs removidos
Nenhum removido. Avaliados e mantidos/combinados:

| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API `month=0` vs `month=13` vs "ano absurdo" (quirk) | Combinados em CT-014 | Mesmo bug (regra #9), mesma asserção de 500; parametrizável |
| API contrato sucesso "sem dados" vs "com dados" | Manter ambos | Shapes distintos (`nextInvoice: null` vs objeto) — dois contratos P0 |
| UI 4 ações rápidas (`quickaction-{expense,income,transfer,savings}`) | Combinados em CT-009 | Todos sem `onClick`, mesma nota `[-]` |
| UI `view-all` vs `view-planning` | Manter separados | Áreas/`data-testid` distintos e condições de render diferentes |
| Contrato CT-001..003 | Manter intactos | Tipo `Contrato` nunca é removido/combinado |

## CTs combinados
1. **API (2):** 3 CTs de quirk (`month=0`, `month=13`, ano absurdo) → 1
   parametrizado (**CT-014**) — mesmo comportamento 500 documentado como bug.
2. **UI (3):** 4 CTs de ações rápidas → 1 (`**CT-009**`) — todos botões mortos.

## Cobertura
- **CAs cobertos (API):** 8/8
- **CAs não cobertos (API):** nenhum
- **Regras cobertas (API):** 9/9
- **CAs cobertos (UI):** 4/4
- **Regras cobertas (UI):** 6/7 — regra 5 (botões mortos) coberta apenas por
  CT-007..010 `[-]`; regra 6 (empty state) por CT-005 ([ ]); regra 7
  (orçamentos do mês) por CT-006

## CTs sem rastreabilidade
Nenhum.

## Gaps encontrados

| Gap | Ação |
|-----|------|
| Quirk `month=13 → 500` sem validação | CT-014 (P2) tratado como **caso executável** de regressão do bug — não `[-]`, para sinalizar em pipeline sem ser correção de app |
| Contrato `nextInvoice` nulo vs preenchido | Dois contratos de sucesso (CT-001/CT-002) — shapes distintos |
| Métrica derivada "livre para gastar" só no front | CT-004 UI adicionado (regra UI #5 derivada de `totalBalance − usedLimit`) |
| Botão `dashboard-budgets-view-more-btn` | Coberto dentro de CT-008 (mesma área) |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| - | - | - | Nenhuma |

## Observações
- `totalBalance` independe do período (Σ contas ACTIVE); `income`/`expenses`/
  `expensesByCategory`/`budgets` dependem do período — não confundir nas
  asserções.
- `expenses` inclui parcelas de cartão; pagamento de fatura não gerra despesa
  (não cria Transaction) — CT-007.
- `nextInvoice` = primeira fatura OPEN por `referenceMonth ASC`
  (`PageRequest.of(0,1)`).
- Quirk de `/dashboard` (500) difere de `/budgets?month=13` (200 vazio) —
  inconsistência documentada em ambas as features.
- Contratos apontam para `schemas/dashboard/dashboard-response.json` e
  `schemas/common/error-response.json` (a criar).
- [SUPOSICAO] CT-012/CT-013 API (complemento de período) e CT-003 UI (year
  isolado): comportamentos parametrizáveis — validar empiricamente.