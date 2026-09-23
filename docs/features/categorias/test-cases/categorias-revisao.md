# Relatório de Revisão - Categorias

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/categorias-api.md`, `test-cases/categorias-ui.md`

## Resumo da revisão
- **CTs originais (API):** 29
- **CTs originais (UI):** 10
- **CTs removidos (duplicados):** 1 (API); 0 (UI)
- **CTs combinados:** 1 (API); 0 (UI)
- **CTs finais (API):** 27
- **CTs finais (UI):** 10

## CTs removidos
| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API: "Atualizar categoria com name > 100 retorna 400" | Remover | Duplica a validação de `name.tooLong` já coberta no POST (CT-022); mesmo `@Size(max=100)` e mesmo shape de erro — manter só o create mantém rastreabilidade com CA `name-too-long` |
| API: CT-018 vs CT-019 (404 inexistente vs ownership) | Manter ambos | CAs distintos — ownership é 404 e não 403 (comportamento relevante próprio) |
| Contrato CT-001..010 | Manter intactos | Tipo `Contrato` nunca é removido/combinado; PATCH 204 fica sem schema (validação de status) |

## CTs combinados
| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API: "Listar com ?type=INCOME" + "Listar com ?type=EXPENSE" | Combinar (CT-013 API) | Mesma regra #2, mesmo endpoint e igual shape varia apenas o valor do filtro; um caso parametrizado basta |
| API: validação 400 (CT-020/021/023) | Manter separados | CAs e campos distintos (`fields`) — combinar perderia rastreabilidade |

## Cobertura
- **CAs cobertos (API):** 12/12 — todos os CAs do `categorias.md`
- **CAs não cobertos (API):** nenhum (uso de categoria em transação coberto em **transacoes**)
- **Regras cobertas (API):** 6/8 direta — regras 6 e 7 (categoria inativa / tipo
  incompatível em transação) cobertas por CTs da feature **transacoes** (409)
- **CAs cobertos (UI):** 7/7
- **Regras cobertas (UI):** 6/6 (regra 6 via CT-010 UI + CT-025 API)

## CTs sem rastreabilidade
Nenhum. CT `[-]` (CT-026/CT-027) referenciam as regras 5 (sem delete / terminal).

## Gaps encontrados

| Gap | Ação |
|-----|------|
| Nenhum CT automatizado (`0%`) | Contratos e fluxos dependem de infra de schemas (`schemas/categorias/*`, `schemas/common/error-response.json`) e de `CategoryClient`/fixture completos |
| 401 em `/categories` só coberto via feature autenticacao | Gap-benigno: cobertura existe em outra feature (dedupe) |
| Filtro `?type=` inválido (CT-024) e `type` inválido no corpo (CT-023) parecem similares | Mantidos separados: um é query param (`BAD_REQUEST` por mismatch de tipo), outro é corpo (enum) — CAs distintos |
| Uso de categoria inativa/tipo incompatível (regras 6/7) | Coberto na feature **transacoes** — referenciado no `categorias.md` |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| - | - | - | Nenhuma |

## Observações
- Contratos (CT-001..010) apontam para `schemas/categorias/category-response.json`,
  `category-list-response.json` (array) e `schemas/common/error-response.json` — todos a criar.
- Contrato do PATCH (CT-009) não tem schema (response 204 vazio); validar status/headers.
- UI: filtro não tem testid próprio de "aplicar" — aplicar = trocar o valor do
  `categories-list-filter` (dispara recarga automática).
- Mensagens de sucesso UI via i18n (`categories.*`) — assert pelo texto renderizado.
- [SUPOSICAO] CT-021 (validação no PUT) usa o mesmo shape de erro do POST; validar
  empiricamente ao implementar.