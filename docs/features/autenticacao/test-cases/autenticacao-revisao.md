# Relatório de Revisão - Autenticação

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/autenticacao-api.md`, `test-cases/autenticacao-ui.md`

## Resumo da revisão
- **CTs originais (API):** 16
- **CTs originais (UI):** 11
- **CTs removidos (duplicados):** 0
- **CTs combinados:** 0
- **CTs finais (API):** 16
- **CTs finais (UI):** 12

## CTs removidos
Nenhum. CTs aparentemente similares foram avaliados e mantidos por CA/regra distintos:

| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API CT-005 vs CT-006 | Manter ambos | Cenários distintos (identifier=username vs identifier=email) e CAs distintos |
| API CT-013 vs CT-014 | Manter ambos | Mesma regra #7, mas CAs distintos (email malformado vs senha curta) — combinar perderia rastreabilidade |
| API CT-015 vs CT-016 | Manter ambos | Cenários distintos (token ausente vs token inválido), prioridades distintas (P1/P2) |
| Contrato CT-001..004 | Manter intactos | Tipo `Contrato` nunca é removido/combinado |

## CTs combinados
Nenhum. CT-012 (API) e CT-006/CT-009 (UI) já nasceram parametrizados na geração.

## Cobertura
- **CAs cobertos (API):** 9/10 — todos; `CA-token-ausente` coberto por CT-015 + CT-016
- **CAs não cobertos (API):** nenhum
- **Regras cobertas (API):** 8/9 — regra 9 (sessão stateless) coberta indiretamente via CT-015/CT-016
- **CAs cobertos (UI):** 7/7
- **Regras cobertas (UI):** 5/6 após revisão — regra 6 (**logout**) ganhou CT-012; regra 5 coberta por CT-010

## CTs sem rastreabilidade
Nenhum.

## Gaps encontrados

| Gap | Ação |
|-----|------|
| UI regra 6 (logout limpa `localStorage`) sem cobertura | Adicionado **CT-012 UI** — "Logout na navbar limpa a sessão e redireciona para /login" (P2) |
| API executado (6%) | Contratos CT-002/003/004 e fluxos P1 pendentes — dependem de infra de schemas de erro/register e de `RegisterRequest` estrito no client |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| - | - | - | Nenhuma |

## Observações
- Contratos de erro (CT-002/CT-004) apontam para `schemas/auth/error-response.json` (existe e implementado) — shape `{timestamp,status,error,message,path,fields?}` do `ApiError`, com `fields` opcional (só presente em 400 `VALIDATION_ERROR`).
- Contrato do register sucesso (CT-003) aponta para `schemas/auth/register-response.json` (existe e implementado).
- `RegisterRequest` atual do client (fixture) usa `name/username/email/password/confirmPassword` — alinhado ao contrato registrado em `autenticacao.md`.
- Mensagens de validação UI referenciadas por chave i18n (`login.*`) — o assert deve usar a tradução renderizada; conferir `src/i18n` ao automatizar.
- [SUPOSICAO] em CT-012/CT-013 (API) e CT-006 (UI): comportamentos parametrizáveis — validar empiricamente ao implementar.