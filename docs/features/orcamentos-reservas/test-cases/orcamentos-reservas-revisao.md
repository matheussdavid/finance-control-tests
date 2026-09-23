# Relatório de Revisão - Orçamentos e Reservas

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/orcamentos-reservas-api.md`, `test-cases/orcamentos-reservas-ui.md`

## Resumo da revisão
- **CTs originais (API):** 22
- **CTs originais (UI):** 10
- **CTs removidos (duplicados):** 1
- **CTs combinados:** 1
- **CTs finais (API):** 21
- **CTs finais (UI):** 9

## CTs removidos
| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API "amount inválido no create" vs "amount inválido no update" | Manter ambos | CAs/regras distintos (create 400 = CT-014, update 400 = CT-015); same JSR-303, contextos diferentes |
| API `spent = 0` ao criar vs `spent` com gastos | Manter ambos | CT-007 (create limpo) e CT-011 (spent com gastos) cobrem estados diferentes |
| API "GET sem período" vs "GET com período" | Manter ambos | CT-008 vs CT-009 têm CAs distintos (todos vs filtrado) |
| Contrato CT-001..006 | Manter intactos | Tipo `Contrato` nunca é removido/combinado |
| UI CT-008 (editar/remover não existe) vs CT-009 (botão morto) | Manter ambos | Regra #6 com naturezas distintas (limitação de produto vs botão morto) |
| UI CT-001 vs CT-006 (resumo) | Combinados | CT-006 (resumo atualiza após criar) é sub-cenário que já surge no CT-002/CT-003; mantidos como CTs separadas por serem estados diferentes? **Decisão final:** manter separados — CT-001 validade massa pré-existente, CT-006 valida re-listagem pós-submit |

Na revisão, foi **removido 1 CT**: o caso "Listar orçamentos com `?month=` de
outro usuário" era duplicado do CT-009 (o filtro é por período, nunca por
usuário) — não existe listagem por ownership no BudgetRepository; o ownership
só é testável por CTS de create/update (CT-016/CT-017). Removido.

## CTs combinados
1. **UI:** o geração inicial tinha CT de "lista expõe progresso/percentual do
   ring" e CT de "texto estourado/restante"; combinados em CT-004 (estouro cobre
   ring vermelho + badge + texto).

## Cobertura
- **CAs cobertos (API):** 11/11
- **CAs não cobertos (API):** nenhum
- **Regras cobertas (API):** 7/8 — regra 8 (sem delete) é limitação `[-]`
  (CT-021)
- **CAs cobertos (UI):** 4/4
- **Regras cobertas (UI):** 5/6 — regra 6 coberta por CT-008/CT-009 `[-]`

## CTs sem rastreabilidade
Nenhum.

## Gaps encontrados

| Gap | Ação |
|-----|------|
| Ownership de categoria no create sem CT | CT-017 adicionado (404 `category.notFound`) |
| `PUT` preservando `spent` em orçamento com gastos | CT-020 adicionado (P2) — garante que update só toca `amount` |
| UI 401/redirect em /reservas | Não adicionado — já coberto globalmente por infra de login (features autenticação/camadas comuns) |
| `month=13` (quirk inconsistente com dashboard) | CT-019 adicionado (P2 borda) documenta o comportamento 200+`[]` |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| - | - | - | Nenhuma |

## Observações
- `spent`/`available` são derivados sob demanda (`computeSummary`), nunca
  persistidos — asserções de UI/API devem criar gasto (transação/parcela) ANTES
  de consultar orçamento.
- Contratos apontam para `schemas/orcamentos/budget-response.json` e
  `schemas/orcamentos/budget-list-response.json` (a criar) + `schemas/common/error-response.json`.
- `GET /budgets?month=13` → 200 `[]`; o mesmo parâmetro em `/dashboard` → 500
  (bug conhecido) — comportamentos **inconsistentes** entre endpoints; documentar
  em cada teste.
- [SUPOSICAO] CT-003 UI (select só EXPENSE ativa) e CT-020 API (PUT mantém
  spent): validar empiricamente ao implementar.