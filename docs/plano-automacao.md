# Plano de Automação — finance-control-tests

Guia geral da ordem em que as features da aplicação **finance-control** devem ser
automatizadas. Cada feature tem um documento próprio com os casos de teste em
`docs/features/<feature>/` (gerados pelas skills `test-case-generator` e
`test-case-reviewer`).

## Objetivo

Automatizar as features em **ordem de dependência** (uma feature só depende do que
já foi automatizado antes) e por **prioridade de negócio** (P0 primeiro). Cada
feature entrega casos de teste revisados e priorizados, e os testes Java
correspondentes crescem junto — contrato → fluxo principal → demais cenários.

## Convenções

- Métodos de teste: `deve<Acao>...` — pt-BR sem acento, verbo no presente
  (ex.: `deveAutenticarUsuarioComCredenciaisValidas`).
- `@DisplayName`: descrição legível, **sem** códigos nem IDs sequenciais
  (CT-001 etc.) — caso novo entra ou sai sem renumeração de nada.
- Ordem dos testes de API em cada classe: **contrato primeiro** (JSON Schema),
  depois o fluxo principal, depois os demais cenários.
- Prioridade dos casos: **P0** (crítico/funcionalidade principal) → **P1**
  (importante, com workaround) → **P2** (borda/moderado) → **P3** (opcional/cosmético).
- Massa dinâmica (`UserFaker`), estado criado via API (fixtures), sem valores fixos.
- Tags: `api|web|contract|smoke`. `smoke` apenas em P0/P1 de caminho feliz.

## Como usar

1. Escolha a próxima feature da lista abaixo (menor ordem com status `[ ]`).
2. Escreva `docs/features/<feature>/<feature>.md` — requisitos (descrição,
   endpoint/rotas, regras de negócio API+UI, critérios de aceite) com base no
   inventário técnico do app.
3. Rode `test-case-generator` → gera `test-cases/<feature>-api.md` e `-ui.md`
   (contratos P0 abrindo o arquivo API).
4. Rode `test-case-reviewer` → deduplica, valida cobertura e gera
   `test-cases/<feature>-revisao.md`.
5. Automatize os casos em Java seguindo as convenções; marque `[x]` no doc.

## Mapa de dependências

```
auth (register/login)
  └─ accounts · categories          (independem entre si)
       └─ transactions              (usa conta + categoria do tipo certo)
            ├─ transfers            (2 contas ACTIVE)
            ├─ credit-cards
            │    └─ purchases       (cartão + categoria EXPENSE)
            │         └─ invoices   (nasce da compra; ciclo OPEN→CLOSED→PAID)
            ├─ budgets              (categoria EXPENSE; spent usa transações + parcelas)
            ├─ dashboard            (agrega contas/transações/cartões/orçamentos/fatura)
            └─ planning             (view composta: dashboard + budgets + invoices)
```

## Ordem de automação

| # | Feature | Doc | Depende de | Camada prevista | Status |
|---|---------|-----|-----------|-----------------|--------|
| 1 | **Autenticação** (register/login) | `docs/features/autenticacao/` | — | API · contrato · Web | `[x]` |
| 2 | **Contas** (accounts) | `docs/features/contas/` | autenticação | API · Web | `[ ]` |
| 3 | **Categorias** (categories) | `docs/features/categorias/` | autenticação | API · Web | `[ ]` |
| 4 | **Transações** (transactions) | `docs/features/transacoes/` | contas · categorias | API · contrato · DB · Web | `[x]` (persistência) |
| 5 | **Transferências** (transfers) | `docs/features/transferencias/` | contas | API · Web | `[ ]` |
| 6 | **Cartões** (credit cards) | `docs/features/cartoes/` | autenticação | API · Web | `[ ]` |
| 7 | **Compras** (purchases) | `docs/features/compras/` | cartões · categorias | API · Web | `[ ]` |
| 8 | **Faturas** (invoices) | `docs/features/faturas/` | compras · contas | API | `[ ]` |
| 9 | **Orçamentos/Reservas** (budgets) | `docs/features/orcamentos-reservas/` | categorias · transações/compras | API · Web | `[ ]` |
| 10 | **Dashboard** | `docs/features/dashboard/` | tudo acima | API · Web | `[ ]` |
| 11 | **Planejamento** (planning) | `docs/features/planejamento/` | dashboard · budgets · faturas | Web (E2E depois) | `[ ]` |

Status: `[x]` automatizado · `[ ]` planejado · `[-]` não automatizável.

## Limitações do app (não automatizar)

Fatos levantados no código-fonte — impedem ou condicionam a automação de certos
cenários:

- **Faturas sem tela no front**: `Pages/InvoicesPage` existe mas **não está
  roteada** — ciclo de fatura só é testável via API.
- **Telas órfãs**: `TransactionsPage` e `BudgetsPage` idem (código morto).
- **Botões mortos no dashboard**: `dashboard-quickaction-*`, `*-view-all`,
  `*-view-planning`, `dashboard-empty-*` — sem `onClick`.
- **Filtro `TRANSFER` em `/gastos`** → 400 (o `TransactionType` só tem
  INCOME/EXPENSE).
- **`/dashboard?month=13`** → 500 (sem validação de faixa; inconsistente com
  `/budgets?month=13` que retorna vazio).
- **Sem update/delete**: transações, transferências, compras e faturas são só
  criação+leitura. Orçamento sem DELETE.
- **Sem create manual de fatura** — nasce apenas via compra.
- **Desativação é terminal**: sem endpoint de reativação; desativar já inativo → 409.
- **`register` com senhas diferentes → 409** (e não 400, como validação de forma).
- **`UserSummary` não retorna username** (só id/name/email).

Essas restrições viram notas `[-]` nos docs de cada feature.