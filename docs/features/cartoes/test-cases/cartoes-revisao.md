# Relatório de Revisão - Cartões

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/cartoes-api.md`, `test-cases/cartoes-ui.md`

## Resumo da revisão
- **CTs originais (API):** 32
- **CTs originais (UI):** 15
- **CTs removidos (duplicados):** 1 (API)
- **CTs combinados:** 0
- **CTs finais (API):** 32
- **CTs finais (UI):** 16

## CTs removidos

| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API CT-032 vs CT-031 | **Removido e substituído** | CT-032 era duplicado do CT-031 (sem reativação de cartão); slot reusado para gap: `PATCH .../deactivate` com `id` não-UUID → 400 BAD_REQUEST |
| UI CT-015 | Mantido (`[-]`) | Limitação distinta de exclusão (CT-016 novo) |
| Contrato CT-001..014 | Mantidos intactos | Tipo `Contrato` nunca é removido/combinado |

## CTs combinados
Nenhum. CTs aparentemente similares foram avaliados e mantidos por CA/regra distintos:

| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API CT-009 vs CT-015 | Manter ambos | Criar vs desativar — CAs distintos |
| UI CT-002 vs CT-004 | Manter ambos | Resumo do cartão vs lista da compra — CAs distintos |

## Cobertura
- **CAs cobertos (API):** 11/11 após revisão — `CA-id-nao-uuid` adicionado pelo gap CT-032
- **CAs cobertos (UI):** 9/9 após revisão — `CA-sem-exclusao-ui` adicionado pelo gap CT-016
- **Regras cobertas (API):** 13/13 — regra 1 corrigida no CT-016
- **Regras cobertas (UI):** 9/9 — regra 1 corrigida no CT-010

## CTs sem rastreabilidade
Nenhum.

## Gaps encontrados

| Gap | Ação |
|-----|------|
| CT-016 (API) assegurava `usedLimit == creditLimit` — **incorreto**: `usedLimit` = soma de parcelas OPEN, `availableLimit` = creditLimit − usedLimit | CT-016 **corrigido** (P1, regra 1) |
| CT-010 (UI) assegurava resumo somando **todos** os cartões — **incorreto**: filtra só `ACTIVE` | CT-010 **corrigido** (P1, regra 1) |
| `id` não-UUID em `deactivate` sem CT | **CT-032 API** adicionado — 400 BAD_REQUEST (P3) |
| Sem edição/exclusão de cartão/compra na UI sem cobertura | **CT-016 UI** adicionado — `[-]` (P3) |
| Limitações com status `[ ]` em vez de `[-]` | CT-031 (API) e CT-015 (UI) marcados `[-]` |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| API CT-032 | P3 (duplicado) | P3 (gap novo) | Mantida a prioridade; escopo trocado |
| UI CT-016 | — | P3 | Novo gap de limitação |

## Observações
- Contratos (CT-001..014) dependem de `schemas/credit-cards/*` e `schemas/common/error-response.json` (a criar).
- `usedLimit`/`availableLimit` são calculados dinamicamente no `CreditCardServiceImpl` — nunca iguais ao cadastro bruto.
- Resumo UI (`CreditCardsPage.tsx:76`) filtra `ACTIVE` antes do reduce.
- Reativação de cartão não existe (desativação terminal) — limitação `[-]` no CT-031.
- Mensagens pt-BR: `creditCard.notActive`, `creditCard.alreadyInactive` (`messages.properties`).
