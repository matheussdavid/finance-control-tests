# Relatório de Revisão - Planejamento

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/planejamento-api.md`, `test-cases/planejamento-ui.md`

## Resumo da revisão
- **CTs originais (API):** 5
- **CTs originais (UI):** 8
- **CTs removidos (duplicados):** 2
- **CTs combinados:** 1
- **CTs finais (API):** 3
- **CTs finais (UI):** 7

## CTs removidos
| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API "CT de composição dashboard" vs "CT de composição budgets" vs "CT de composição invoices" isolados | Removidos (2) | Sem endpoint próprio, cada "composição isolada" duplica a cobertura das features de origem; mantidas 3 referências `[-]` genéricas de rastreamento |
| Contrato de API do planejamento | Removido (nulo) | Sem endpoint → sem schema próprio; reutilizar schemas das features de origem (planejamento-api CT-003 cobre como referência) |

## CTs combinados
1. **UI:** alterar mês vs alterar ano (refaziam as 3 chamadas) → **CT-005 único**
   parametrizado.

## Cobertura
- **CAs cobertos (API):** N/A (feature sem CA de API próprio — só referências)
- **Regras cobertas (API):** 0/1 registrada como nota (`-` em CT-001..003)
- **CAs cobertos (UI):** 4/4
- **Regras cobertas (UI):** 5/6 — regra 6 (estado depende de massa de API) é
  pré-condição transversal, sem CT dedicado

## CTs sem rastreabilidade
Nenhum.

## Gaps encontrados

| Gap | Ação |
|-----|------|
| Automação declarada como fase E2E | Documentado em `planejamento.md`; casos UI são de integração/visual com pré-estado via API |
| Sem endpoint próprio: risco de duplicar CTs de API | Resolvido reduzindo `planejamento-api.md` a 3 casos `[-]` de referência apontando para as features de origem |
| Composição não assertável em API | Adicionada nota em CT-002 (efeito somado já assertado na origem) |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| - | - | - | Nenhuma |

## Observações
- Nenhum schema em `schemas/planejamento/*` — reutilizar `schemas/dashboard/*`,
  `schemas/orcamentos/*`, `schemas/faturas/*`.
- Estado da UI sempre criado via API antes da navegação (transações, compras,
  faturas, orçamentos). Sem isso não há o que exibir.
- `planning-overview-invoices` soma apenas faturas `OPEN` (filtro client por
  status); `planning-projection-balance` = `income − expenses` do dashboard.
- A navegação `/planejamento` exige auth — login via `TestUserFixture`.
- [SUPOSICAO] CT-005 (mês e ano parametrizáveis) e CT-006 (empty states):
  validar textos reais em `src/i18n` ao implementar.