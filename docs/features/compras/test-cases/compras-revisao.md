# Relatório de Revisão - Compras

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/compras-api.md`, `test-cases/compras-ui.md`

## Resumo da revisão
- **CTs originais (API):** 31
- **CTs originais (UI):** 10
- **CTs removidos (duplicados):** 1 (API)
- **CTs combinados:** 0
- **CTs finais (API):** 31
- **CTs finais (UI):** 12

## CTs removidos

| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API CT-031 vs CT-029 | **Removido e substituído** | CT-031 era duplicado do CT-029 (sem update/delete de compra); slot reusado para gap: clamp de dia (dueDay 31 em fevereiro → 28/29) |
| UI CT-010 vs CT-002 | Manter ambos | CT-002 (positivo, P0) vs CT-010 (borda, P2) — CAs distintos após correção |
| Contrato CT-001..016 | Mantidos intactos | Tipo `Contrato` nunca é removido/combinado |

## CTs combinados
Nenhum. CTs aparentemente similares foram avaliados e mantidos por CA/regra distintos:

| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API CT-011 vs CT-017 | Manter ambos | Limite aceito (positivo) vs fatura fechada (negativo) — CAs distintos |
| UI CT-001 vs CT-007 | Manter ambos | Sucesso da compra vs exibição na lista — CAs distintos |

## Cobertura
- **CAs cobertos (API):** 11/11 após revisão — `CA-clamp-dia` adicionado pelo gap CT-031
- **CAs cobertos (UI):** 9/9 após revisão — `CA-sem-tela-fatura` (CT-011) e `CA-sem-edicao-compra` (CT-012) adicionados
- **Regras cobertas (API):** 11/11 — regra 7 corrigida no CT-019
- **Regras cobertas (UI):** 8/8 — CT-002 corrigido

## CTs sem rastreabilidade
Nenhum.

## Gaps encontrados

| Gap | Ação |
|-----|------|
| CT-019 (API) assegurava **404** para fatura fechada — **incorreto**: status real é **409** `purchase.cannotAddToInvoice` | CT-019 **corrigido** (P1, regra 7) |
| CT-002 (UI) assegurava "resumo não muda após compra" — **incorreto**: resumo **atualiza** (used +V, available −V) | CT-002 **corrigido** (P0, regra 4) |
| Clamp de dia (fatura dia 31 em fevereiro) sem CT | **CT-031 API** adicionado — 28/29 via `withDayClamped` (P3) |
| Tela de fatura inacessível (InvoicesPage órfã, sem rota) sem cobertura | **CT-011 UI** adicionado — `[-]` (P3) |
| Sem edição/exclusão de compra na UI sem cobertura | **CT-012 UI** adicionado — `[-]` (P3) |
| Limitações com status `[ ]` em vez de `[-]` | CT-029/CT-030 (API) marcados `[-]` |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| API CT-031 | P3 (duplicado) | P3 (gap novo) | Mantida a prioridade; escopo trocado |
| UI CT-011/CT-012 | — | P3 | Novas limitações documentadas |

## Observações
- Contratos (CT-001..016) dependem de `schemas/purchases/*` e `schemas/common/error-response.json` (a criar).
- Fatura gerada automaticamente por compra; fechamento via `POST /invoices/{id}/close` usado como pré-condição.
- Rounding: 100/3 → 33.33/33.33/33.34 (DOWN, resto na última parcela).
- 1ª fatura: `purchaseDate.day <= closingDay` → mês da compra, senão mês seguinte; clamp em `withDayClamped`.
- `InvoicesPage` existe no código mas **não está roteada** em `AppRoutes.tsx` — verificação por inspeção.
- Mensagens pt-BR: `purchase.insufficientLimit`, `purchase.cannotAddToInvoice`, `category.mustBeExpenseForPurchase` (`messages.properties`).
