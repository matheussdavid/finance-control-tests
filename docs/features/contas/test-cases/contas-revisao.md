# Relatório de Revisão - Contas

**Data da revisão:** 2026-09-24
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/contas-api.md`, `test-cases/contas-ui.md`

## Resumo da revisão
- **CTs originais (API):** 26
- **CTs originais (UI):** 10
- **CTs removidos (duplicados):** 0 (API); 0 (UI)
- **CTs combinados:** 0 (API); 1 (UI — mensagens fundidas)
- **CTs adicionados:** 0 (API); 1 (UI — CT-007, erro de API / regra 6)
- **CTs finais (API):** 26
- **CTs finais (UI):** 10
- **Mudança estrutural:** API e UI reescritos com agrupamento por endpoint/página (convenção das skills atualizadas), CTs renumerados sequencialmente; BVA formalizado.

## CTs removidos
Nenhum CT funcional foi removido. Mudanças de estrutura/conteúdo:

| Analisados | Decisão | Motivo |
|------------|---------|--------|
| UI antigo CT-007 (mensagens criar/atualizar/desativar combinadas) | Fundido nos happy paths | Assert da mensagem (CA #3/regra 3) virou passo de CT-001/CT-005/CT-006 — elimina CT transversal, mantém rastreabilidade e cobertura |
| API antigo CT-020 (saldo negativo -1) | Reclassificado → CT-005 BVA | Saldo virou Borda com limite exato `min-1=-0.01` / `min=0.00` (era -1, fora da borda) |
| API antigo CT-021 (name >100) | Reclassificado → CT-006 BVA | Nome virou Borda com `max=100` (aceito) / `max+1=101` (rejeitado) — antes só testava 101 |
| API CT-001..010 contratos | Manter intactos | Tipo `Contrato` nunca é removido/combinado; PATCH 204 segue sem schema (validação de status/corpo vazio) |
| API 401 em `/accounts` | Manter só como contrato de erro (CT-009) | Dedupe mantido: 401 funcional coberto pela feature **autenticacao** (CT-015) — aqui é apenas gatilho de forma do schema de erro |

## CTs combinados
| IDs originais | Novo ID | Motivo |
|---------------|---------|--------|
| UI CT-007 (mensagens) | fundido em CT-001/CT-005/CT-006 | Mesma regra #3/CA-ui-msg, aplicada por fluxo; evitar CT transversal no agrupamento por página |
| UI CT-003 (saldo branco + negativo) | CT-003 (mantido) | Já era parametrizado na geração anterior; agora com valores BVA explícitos (-0.01/-10) |
| API 400 de validação do POST | CT-004 mantido separado | Campos distintos (`fields.name/type/initialBalance`) — combinar perderia rastreabilidade |

## CTs adicionados (gaps fechados nesta revisão)
| ID | Origem | Motivo |
|----|--------|--------|
| UI CT-007 | Regra 6 sem cobertura | Erro de API exibido no `Message` (`formatApiError`) não tinha CT — desativação em segundo plano via API força o 409 na UI (único caminho de erro de API alcançável no fluxo) |
| API CT-005/CT-006 BVA | Skill exige BVA nos limites | `@DecimalMin("0.0")` e `@Size(max=100)` conferidos no `AccountRequest` do backend; bordas aceitas agora são testadas (0.00 e 100) além das rejeitadas |

## Cobertura
- **CAs cobertos (API):** 13/13 — todos os CAs do `contas.md`
- **CAs não cobertos (API):** nenhum
- **Regras cobertas (API):** 7/8 direta — regra 6 (conta inativa bloqueia movimentação) coberta por CTs da feature **transacoes** (409)
- **CAs cobertos (UI):** 10/10 — inclui novo `CA-ui-erro-api`
- **Regras cobertas (UI):** 6/6 — regra 6 agora coberta por CT-007

## CTs sem rastreabilidade
Nenhum. CTs `[-]` (CT-025/CT-026 API) referenciam as regras 8 e 5 e são notas de limitação. CTs de validação (CT-004..007, CT-019) usam `Regra: N/A` — JSR-303 não é regra de negócio numerada; mantêm CA.

## Correções da versão anterior
| Erro antigo | Correção |
|-------------|----------|
| Validations no POST/PUT apontavam regra **#8** | `Regra: N/A` — validação não é regra de negócio numerada; #8 é "sem delete físico" (CT-025) |
| CT-025 (sem delete) apontava **#9** | Regra #8 (só existem 8 regras no `contas.md`) |
| Saldo negativo/name >100 sem BVA | CTs de borda com valores de limite exatos (0.00/-0.01 e 100/101) |
| Sem agrupamento por endpoint/página | API agrupada por `### VERBO /path`; UI por `### AccountsPage (modo)` |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| API CT-006 BVA name | (novo) | P2 | Borda de comprimento alternativo — mesmo peso do antigo name>100 |
| API CT-005 BVA saldo | (novo) | P1 | Limite do campo de dinheiro na criação — mantém peso do antigo saldo negativo |
| UI CT-007 erro API | (novo) | P2 | Erro de API é cenário secundário na UI (só via estado divergente) |

## Gaps encontrados

| Gap | Ação |
|-----|------|
| Nenhum CT automatizado (`0%`) | Próximo passo: criar `AccountClient`, builder/fixture de conta e schemas de contrato (`schemas/contas/*`, `schemas/common/error-response.json`) antes da automação |
| Schemas de contrato não existem | `schemas/contas/account-response.json`, `account-list-response.json` e `schemas/common/error-response.json` a criar (shape `{timestamp,status,error,message,path,fields?}` — `fields` opcional) |
| UI regra 6 só alcançável por estado divergente | CT-007 documenta o truque (desativar via API antes do clique); se o `status` refetcher da página impedir o stale, marcar `[-]` |

## Observações
- Contratos (CT-001,002,008,009,011,012,016,017,020,021) P0 — um por shape sucesso/erro de cada endpoint; PATCH 204 sem schema (validação de status/corpo vazio).
- Contrato de erro usa gatilhos ricos por endpoint: POST `{}` → 400 com `fields`; GET lista sem token → 401; GET/PUT id inexistente → 404; PATCH inativa → 409.
- Erro de negócio/404/401 omitem `fields` (serializador `non_null`) — só 400 `VALIDATION_ERROR` traz `fields`. Replicar nota no schema de erro.
- BVA conferido no backend (`AccountRequest.java:15,24`) — limites reais mandam. `type` é enum, sem BVA de comprimento (decisão no Resumo Executivo do API).
- `AccountRequest` (POST) pede `initialBalance`; `AccountUpdateRequest` (PUT) não — dois records distintos no client.
- CT-024 (saldo negativo) usa `POST /transactions` como apoio — depende dos clients/fixture de transações (feature já automatizada).
- [SUPOSICAO] CT-019 (validação no PUT) usa o mesmo shape de erro do POST — validar empiricamente ao implementar.
- Mensagens de sucesso/erro UI via i18n (`accounts.*`) — assert pelo texto renderizado; conferir `src/i18n/pt.ts:295-317` ao automatizar.
- CTs renumerados (agrupamento) — referências por `@DisplayName` descritivo em Java, sem IDs; sem impacto.

---