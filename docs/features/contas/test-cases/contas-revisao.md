# Relatório de Revisão - Contas

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/contas-api.md`, `test-cases/contas-ui.md`

## Resumo da revisão
- **CTs originais (API):** 27
- **CTs originais (UI):** 11
- **CTs removidos (duplicados):** 1 (API); 0 (UI)
- **CTs combinados:** 0 (API); 1 (UI)
- **CTs finais (API):** 26
- **CTs finais (UI):** 10

## CTs removidos
| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API: "Endpoint autenticado sem token retorna 401" | Remover | Duplicado da feature **autenticacao** (CT-015 de `autenticacao-api.md`); a proteção 401 de `/accounts` já é coberta lá |
| API: CT-014 vs CT-015 (atualizar vs desativar) | Manter ambos | CAs distintos (edição preserva saldo vs desativação terminal) |
| API: CT-017 vs CT-018 (404 inexistente vs ownership) | Manter ambos | CAs distintos — ownership é 404 e não 403 (comportamento relevante próprio) |
| Contrato CT-001..010 | Manter intactos | Tipo `Contrato` nunca é removido/combinado; PATCH 204 fica sem schema (validação de status) |

## CTs combinados
| Analisados | Decisão | Motivo |
|------------|---------|--------|
| UI: "Saldo inicial em branco" + "Saldo inicial negativo" | Combinar (CT-003 UI) | Mesma validação client (`required`/`min=0`) e mesmo comportamento esperado; um caso parametrizado basta |
| API: validation 400 (CT-019/020/021/023) | Manter separados | CAs e campos distintos (`fields`) — combinar perderia rastreabilidade |

## Cobertura
- **CAs cobertos (API):** 13/13 — todos os CAs do `contas.md`
- **CAs não cobertos (API):** nenhum
- **Regras cobertas (API):** 7/8 direta — regra 6 (conta inativa bloqueia movimentação) coberta por CTs da feature **transacoes** (409)
- **CAs cobertos (UI):** 8/8
- **Regras cobertas (UI):** 6/6

## CTs sem rastreabilidade
Nenhum. CTs `[-]` (CT-025/CT-026) referenciam as regras 9 e 5 e são notas de limitação.

## Gaps encontrados

| Gap | Ação |
|-----|------|
| Nenhum CT automatizado (`0%`) | Contratos e fluxos dependem de infra de schemas (`schemas/contas/*`, `schemas/common/error-response.json`) e de `AccountClient`/fixture completos |
| 401 em `/accounts` só coberto via feature autenticacao | Mantido como gap-benigno: a cobertura existe em outra feature (dedupe) |
| Bloqueio de conta inativa (transação/transferência/fatura) sem CT em contas | Mantido em **transacoes** (natural do fluxo) — referenciado no `contas.md` (regra 6) |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| - | - | - | Nenhuma |

## Observações
- Contratos (CT-001..010) apontam para `schemas/contas/account-response.json`,
  `account-list-response.json` (array) e `schemas/common/error-response.json` — todos a criar.
- Contrato do PATCH (CT-009) não tem schema (response 204 vazio); validar status/headers.
- `AccountRequest` (POST) pede `initialBalance`; `AccountUpdateRequest` (PUT) não —
  o client final precisa de dois records (já espelhados em `api/requests/`).
- Saldo negativo (CT-024) usa transação EXPENSE como pré-condição —
  depende dos clients de transaction/fixture de conta.
- Mensagens de sucesso UI via i18n (`accounts.*`) — assert pelo texto renderizado.
- [SUPOSICAO] CT-023 (validação no PUT) usa o mesmo shape de erro do POST; validar
  empiricamente ao implementar.