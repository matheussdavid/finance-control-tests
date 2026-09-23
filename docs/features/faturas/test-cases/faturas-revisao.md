# Relatório de Revisão - Faturas

**Data da revisão:** 2026-09-22
**Revisor:** QA Agent
**Arquivos revisados:** `test-cases/faturas-api.md`, `test-cases/faturas-ui.md`

## Resumo da revisão
- **CTs originais (API):** 22
- **CTs originais (UI):** 4
- **CTs removidos (duplicados):** 1
- **CTs combinados:** 0
- **CTs finais (API):** 21
- **CTs finais (UI):** 4

## CTs removidos
| Analisados | Decisão | Motivo |
|------------|---------|--------|
| API CT de listagem com `?page=1`/paginado vs CT-010 | Removido | Mesma regra #8; a paginação real é coberta indiretamente pelo contrato de `Page` (CT-001). Sem ganho de comportamento |
| UI CT-001 (navegação) vs demais `[-]` | Manter | Documenta a limitação raiz (tela inacessível) — mantido como borda |

## CTs combinados
Nenhum. Ciclo completo (CT-009) mantido íntegro como único CT P0 de
integração; não foi fatiado em compra/close/pay isolados por serem cenários
dependentes do mesmo estado.

## Cobertura
- **CAs cobertos (API):** 14/14 — todos
- **CAs não cobertos (API):** nenhum
- **Regras cobertas (API):** 8/8
- **CAs cobertos (UI):** 0/0 — feature sem CA de UI automatizável
- **Regras cobertas (UI):** 0/4 — todas cobertas indiretamente pela API
  (dashboard/planejamento refletem fatura; ver features respetivas)

## CTs sem rastreabilidade
Nenhum.

## Gaps encontrados

| Gap | Ação |
|-----|------|
| Ownership em `close`/`pay` sem CT dedicado | CT-020 cobre get/close/pay de fatura de outro usuário (P2, parametrizado) |
| 401 sem depender de schema | CT-021 mantido (P2) para garantir o fluxo independente da infra de schemas |
| Contratos (CT-001 a CT-008) | Todos `[ ]` — dependem de `schemas/faturas/*` e `schemas/common/error-response.json` |

## Alterações de prioridade
| ID | De | Para | Motivo |
|----|----|----|--------|
| - | - | - | Nenhuma |

## Observações
- Ciclo completo (compra → close → pay → débito → marcação PAID) é o CT P0
  central e deve ser o `@Tag("smoke")` da feature.
- Débito na conta não cria `Transaction` — a asserção de `balance` pós-pay deve
  comparar saldo anterior − `totalAmount` (via `GET /accounts/{id}`), não via
  histórico de transações.
- `paidAt` é gravado em UTC (`LocalDateTime.now(ZoneOffset.UTC)`) — asserção
  deve tolerar instante corrente e não comparar com hora local do runner.
- UI: `[NA]` registrado como nota de inacessibilidade; quando a rota for
  adicionada, ativar CT-001..004 mudando status `[-]` → `[ ]`.
- A soma das parcelas pode ter arredondamento (`RoundingMode.DOWN` + resto na
  última parcela na compra); usar valores com casas curtas para evitar
  flutuação em asserção de total.
- [SUPOSICAO] CT-020 (ownership parametrizado) e CT-021 (401) — validar
  empiricamente ao implementar.