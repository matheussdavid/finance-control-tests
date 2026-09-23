# Relatório de Revisão - Transações

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/transacoes-api.md`, `test-cases/transacoes-ui.md`

## Resumo da revisão
- **CTs originais (API):** 29
- **CTs originais (UI):** 14
- **CTs removidos (duplicados):** 0 (API); 0 (UI)
- **CTs combinados:** 0 (API); 1 (UI)
- **CTs finais (API):** 29
- **CTs finais (UI):** 13

## CTs removidos
Nenhum. CTs aparentemente similares foram avaliados e mantidos por CA/regra distintos:

| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API CT-013 vs CT-014 (conta inativa vs categoria inativa) | Manter ambos | Mensagens distintas (`account.notActive` vs `category.notActive`), regras diferentes (#2/#3) |
| API CT-015 vs CT-016 (tipo incompatível) | Manter ambos | Mesma regra #4, mas CAs e mensagens distintos (`category.mustBeIncome` vs `mustBeExpense`) |
| API CT-022 vs CT-023 (type inválido no corpo vs filtro) | Manter ambos | Cenários distintos (corpo vs query param), ainda que ambos 400 `BAD_REQUEST` |
| API CT-027/028/029 (`[-]`) | Manter intactos | Notas de limitação (sem update/delete; pagamento de fatura cross-feature) |
| Contrato CT-001..006 | Manter intactos | Tipo `Contrato` nunca é removido/combinado |

## CTs combinados
| Analisados | Decisão | Motivo |
|------------|---------|--------|
| UI: "Filtro por conta" + "Filtro por categoria" | Combinar (CT-005 UI) | Mesma mecânica de select → recarga com `page=0`; um caso parametrizado basta |
| UI: CT-012 "erro no modal" | Status [ ] → [-] | Fluxo raro: QuickCapture só lista contas/categorias ativas — erro 409 exige corrida entre render e submit; vira nota documental |

## Cobertura
- **CAs cobertos (API):** 15/15 — todos os CAs do `transacoes.md`
- **CAs não cobertos (API):** nenhum (CA-pagamento-fatura coberto por nota `[-]` CT-029)
- **Regras cobertas (API):** 7/9 — regras 5 (sem edit/remove) e 7 (pag. de fatura) como casos `[-]`
- **CAs cobertos (UI):** 8/8
- **Regras cobertas (UI):** 5/7 com CT executável; regra 4 (filtro TRANSFER bug) e regra 5 (quickadd sem pré-seleção) documentadas via CTs (P2 / `[-]`)
- **Persistência DB (API):** 1 CT em `[x]` (CT-009) — único CT já automatizado da bateria

## CTs sem rastreabilidade
Nenhum.

## Gaps encontrados

| Gap | Ação |
|-----|------|
| Contratos (CT-001..006) e quase todos os fluxos pendentes (3% executado) | Dependem de infra de schemas (`schemas/transacoes/*`, `schemas/common/error-response.json`) e de `TransactionClient`/fixtures |
| Persistência DB via `TransactionPersistenceTest` coberta | Mantida como ref (CT-009 `[x]`) — única automação existente para transações |
| Pagamento de fatura não criar Transaction | Nota `[-]` (CT-029) — setup de cartão/compra/fatura fora desta bateria de features |
| Data futura aceita no backend | CT-026 (P3) documenta — o teste da UI (CT-010) cobre a trava `max=today()` do QuickCapture |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| API CT-026 (data futura aceita) | P2 | P3 | Documenta limitação (backend não valida), não é regra crítica — o comportamento esperado do produto é o trava do front |

## Observações
- Contratos apontam para `schemas/transacoes/transaction-response.json`,
  `transaction-page-response.json` (shape `Page` Spring Data) e
  `schemas/common/error-response.json` — todos a criar.
- `GET /transactions` responde `Page` (metadados `content/totalElements/totalPages/number/size`) —
  o contrato do array simples **não** se aplica aqui (diferente de contas/categorias).
- Cross-feature consolidadas aqui: conta inativa (CT-013), categoria inativa e tipo
  incompatível (CT-014..016) — fecham regras dos `.md` de contas/categorias.
- Mensagens pt-BR usadas nos asserts (`messages.properties`):
  `account.notActive`, `category.notActive`, `category.mustBeIncome`,
  `category.mustBeExpense`, `amount.greaterThanZero`.
- [SUPOSICAO] CT-008 (paginação) com `size` default 50 da UI — exige preparo de massa
  ou manipulação de `size` na montagem para tornar a paginação visível; validar ao implementar.