# Relatório de Revisão - Transferências

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/transferencias-api.md`, `test-cases/transferencias-ui.md`

## Resumo da revisão
- **CTs originais (API):** 24
- **CTs originais (UI):** 11
- **CTs removidos (duplicados):** 1 (API)
- **CTs combinados:** 1 (UI)
- **CTs finais (API):** 24
- **CTs finais (UI):** 11

## CTs removidos

| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API CT-024 vs CT-022 | **Removido e substituído** | CT-024 era duplicado literal do CT-022 (sem update/delete); slot reusado para gap: `GET /transfers/{id}` com `id` não-UUID → 400 BAD_REQUEST |
| UI CT-011 vs CT-003 | **Combinado** | Mesmo cenário (conta origem == destino) em form rápido vs modal — fundido no CT-003 como variação parametrizada |
| Contrato CT-001..013 | Mantidos intactos | Tipo `Contrato` nunca é removido/combinado |

## CTs combinados
- **UI CT-003** agora cobre `transfers-quick-form` **e** `transfers-modal-form` em um único caso (mesmo handler/estado).

## Cobertura
- **CAs cobertos (API):** 13/13 após revisão — `CA-id-nao-uuid` criado pelo gap CT-024
- **CAs cobertos (UI):** 9/9 após revisão — `CA-sem-filtros-ui` adicionado pelo novo CT-011
- **Regras cobertas (API):** 10/10 — regra 6 (sem validação de saldo) corrigida no CT-017
- **Regras cobertas (UI):** 9/9 — regra 9 ganhou CT-011

## CTs sem rastreabilidade
Nenhum.

## Gaps encontrados

| Gap | Ação |
|-----|------|
| CT-017 (API) assegurava 409 "saldo insuficiente" — regra **não existe** no app (transferência pode deixar saldo negativo) | CT-017 **corrigido** para 201 + saldo final negativo (P1, regra 6) |
| `id` não-UUID na URL (type mismatch) sem CT | **CT-024 API** adicionado — 400 BAD_REQUEST (P3) |
| Sem filtros na UI sem cobertura | **CT-011 UI** adicionado — `[-]` sem busca/filtros (P3) |
| CT-007 (UI) assumia "data futura aceita" — input tem `max={today()}` | CT-007 **corrigido** — data futura bloqueada, sem chamada à API |
| Limitações com status `[ ]` em vez de `[-]` | CT-022/CT-023 (API) e CT-010/CT-011 (UI) marcados `[-]` |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| API CT-024 | P3 (duplicado) | P3 (gap novo) | Mantida a prioridade; escopo trocado |

## Observações
- Contratos (CT-001..013) dependem de `schemas/transferencias/*` e `schemas/common/error-response.json` (a criar) — shape `{timestamp,status,error,message,path,fields}`.
- Sem `update/delete` de transferência: limitação documentada como `[-]` no CT-022.
- `GET /transfers` sem filtros além de paginação: limitação `[-]` no CT-023.
- Mensagens esperadas em pt-BR via `messages.properties` (`transfer.sameAccount`, etc).
