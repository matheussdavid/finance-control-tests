# História: Faturas

## Descrição

Faturas de cartão de crédito **não têm criação manual**: nascem automaticamente
quando uma compra é feita (`POST /purchases` gera a fatura `OPEN` do mês e as
parcelas). O ciclo de vida é **OPEN → CLOSED → PAID**, irreversível. Não existem
endpoints de update/delete; o confronto com o banco é garantido pela unicidade
`(credit_card_id, reference_month)`.

O fluxo cobre listagem paginada (mais recentes primeiro), detalhe com parcelas,
fechamento (que recalcula o total = Σ parcelas) e pagamento (que debita o saldo
da conta e marca as parcelas como pagas).

**Limitação crítica (UI):** a página `InvoicesPage.tsx` do frontend **não está
roteada** — o arquivo existe e tem `data-testid` completos, mas `AppRoutes.tsx`
não a importa (código morto/inacessível). Faturas são **API-only**: na UI o
usuário só vê fatura via dashboard (`dashboard-next-invoice-card`) e
planejamento (`planning-invoice-*`). Por isso o documento de UI desta feature
contém apenas casos marcados `[-]` com a motivação.

---

## API

### Endpoints

- **`GET /invoices`** → **200 OK** (`Page<InvoiceResponse>`, sort
  `referenceMonth DESC`)
- **`GET /invoices/{id}`** → **200 OK** (`InvoiceDetailResponse` com
  `installments[]`)
- **`POST /invoices/{id}/close`** → **200 OK** (`InvoiceResponse`)
- **`POST /invoices/{id}/pay`** → **200 OK** (`InvoiceResponse`)
- `POST /invoices` (create manual) — **não existe** (fatura nasce via compra)
- `PUT/DELETE /invoices/{id}` — **não existem**

### Request — `POST /invoices/{id}/pay`

Body válido é obrigatório para todo POST. `close` não tem body.

```json
{
  "accountId": "3f7b1c8d-..."   // Obrigatório (@NotNull), UUID da conta de débito
}
```

### Response — `GET /invoices` (página)

```json
{
  "content": [ { "...": "InvoiceResponse" } ],
  "totalElements": 12,
  "totalPages": 2,
  "size": 10,
  "number": 0
}
```

### Response — `InvoiceResponse` (item da lista / close / pay)

```json
{
  "id": "<uuid>",
  "creditCardId": "<uuid>",
  "creditCardName": "Nubank",
  "referenceMonth": "2026-09-01",
  "closingDate": "2026-09-28",
  "dueDate": "2026-10-05",
  "status": "OPEN",
  "totalAmount": 400.00,
  "paidAt": null
}
```

### Response — `GET /invoices/{id}` (`InvoiceDetailResponse`)

Mesmos campos do `InvoiceResponse` +:

```json
{
  "...": "...InvoiceResponse...",
  "installments": [
    {
      "installmentId": "<uuid>",
      "purchaseId": "<uuid>",
      "purchaseDescription": "Notebook",
      "categoryId": "<uuid>",
      "categoryName": "Eletrônicos",
      "number": 1,
      "amount": 33.33,
      "status": "OPEN"
    }
  ]
}
```

### Shape de erro (compartilhado)

```json
{
  "timestamp": "...",
  "status": 409,
  "error": "BUSINESS_RULE_VIOLATION",
  "message": "Apenas faturas abertas podem ser fechadas",
  "path": "...",
  "fields": null
}
```

`fields` (map campo→mensagem) só é preenchido em 400 de validação (ex.:
`accountId` ausente no pay). Erros já chegam traduzidos para pt-BR.

### Status usados nesta feature

| Cenário | HTTP | `error` |
|---|---|---|
| Fatura inexistente ou de outro usuário | 404 | `NOT_FOUND` |
| Conta de débito inexistente ou de outro usuário (pay) | 404 | `NOT_FOUND` |
| Fechar fatura não-OPEN | 409 | `BUSINESS_RULE_VIOLATION` |
| Pagar fatura já paga | 409 | `BUSINESS_RULE_VIOLATION` |
| Pagar fatura não fechada | 409 | `BUSINESS_RULE_VIOLATION` |
| Pagar com conta inativa | 409 | `BUSINESS_RULE_VIOLATION` |
| `accountId` ausente no pay | 400 | `VALIDATION_ERROR` (+ `fields`) |
| Sem token / token inválido | 401 | `UNAUTHORIZED` |

---

## Regras de negócio (API)

1. **Fatura nasce via compra:** não existe `POST /invoices` manual — a compra
   cria a fatura do mês e as parcelas `OPEN`.
2. **Ciclo de status irreversível:** `OPEN → CLOSED → PAID`; sem caminho de
   volta.
3. **Close só em fatura aberta:** fatura não-`OPEN` no close → 409
   (`invoice.onlyOpenCanClose`); o close **recalcula** `totalAmount = Σ
   installments` e grava `CLOSED`.
4. **Pagamento só de fatura fechada:** fatura `PAID` → 409
   (`invoice.alreadyPaid`); fatura `OPEN` → 409 (`invoice.mustBeClosedToPay`).
5. **Conta de débito:** no pay, a conta deve existir e ser do usuário (404
   `account.notFound`, inclui ownership) e estar `ACTIVE` (409
   `account.notActive`).
6. **Efeitos do pay:** marca todas as installments da fatura `PAID`, debita
   `account.balance -= totalAmount`, grava `paidAt` (UTC), status `PAID`. Não
   cria `Transaction` (o pagamento não é contabilizado de novo como despesa).
7. **Ownership:** toda consulta usa `findByUser_IdAndId` — `id` de outro usuário
   → 404 `invoice.notFound` (nunca 403).
8. **Unicidade:** `(credit_card_id, reference_month)` com constraint no banco
   (`uq_invoice_card_month`); `reference_month` = 1º dia do mês.

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** um usuário com conta `ACTIVE` e cartão de crédito `ACTIVE`
- **Quando** crio uma compra parcelada no mês corrente (via API)
- **Então** nasce uma fatura `OPEN` para o mês com `totalAmount = Σ parcelas`
- **Dado** um usuário com várias faturas
- **Quando** envio `GET /invoices`
- **Então** recebo uma `Page` com faturas ordenadas por `referenceMonth` DESC
- **Dado** uma fatura existente com parcelas
- **Quando** envio `GET /invoices/{id}`
- **Então** recebo o detalhe com a lista `installments[]` e seus `amount`/`number`/`status`
- **Dado** uma fatura `OPEN`
- **Quando** envio `POST /invoices/{id}/close`
- **Então** a fatura fica `CLOSED` e `totalAmount` = Σ installments
- **Dado** uma fatura `CLOSED` e uma conta `ACTIVE`
- **Quando** envio `POST /invoices/{id}/pay` com `accountId` da conta
- **Então** a fatura fica `PAID`, `paidAt` preenchido (UTC), as parcelas ficam `PAID` e o saldo da conta é debitado em `totalAmount`

**Ciclo completo (automatizável):**
- **Dado** usuário com conta e cartão
- **Quando** executo o ciclo compra → close → pay
- **Então** a fatura nasce `OPEN`, fecha com total recalculado, paga com débito
  na conta e parcelas marcadas `PAID`

### Casos negativos / borda
- **Dado** uma fatura já `CLOSED` (ou `PAID`)
- **Quando** envio `POST /invoices/{id}/close`
- **Então** recebo 409 `invoice.onlyOpenCanClose`
- **Dado** uma fatura já `PAID`
- **Quando** envio `POST /invoices/{id}/pay`
- **Então** recebo 409 `invoice.alreadyPaid`
- **Dado** uma fatura `OPEN` (não fechada)
- **Quando** envio `POST /invoices/{id}/pay`
- **Então** recebo 409 `invoice.mustBeClosedToPay`
- **Dado** uma conta `INACTIVE`
- **Quando** envio o pay com essa conta
- **Então** recebo 409 `account.notActive`
- **Dado** uma conta de outro usuário
- **Quando** envio o pay com essa conta
- **Então** recebo 404 `account.notFound`
- **Dado** `accountId` ausente no body do pay
- **Quando** envio `POST /invoices/{id}/pay`
- **Então** recebo 400 `VALIDATION_ERROR` com `fields.accountId`
- **Dado** uma fatura inexistente ou de outro usuário
- **Quando** envio `GET /invoices/{id}` (ou close/pay)
- **Então** recebo 404 `invoice.notFound`
- **Dado** sem token ou token inválido
- **Quando** chamo qualquer endpoint de fatura
- **Então** recebo 401 `UNAUTHORIZED`

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/InvoicesPage.tsx`, `frontend/src/routes/AppRoutes.tsx`.

### Status: TELA INACESSÍVEL (API-only)

`InvoicesPage.tsx` contém a tela completa de faturas (listar/fechar/detalhe/
pagar) com `data-testid` funcionais, **mas não está roteada** em
`AppRoutes.tsx` (nenhuma rota aponta para ela; arquivo não é importado) →
código morto. Na navegação real, faturas só aparecem de forma indireta:

- `DashboardPage` — `dashboard-next-invoice-card` (próxima fatura aberta).
- `PlanningPage` — `planning-invoice-{id}` (faturas abertas do mês).

Por isso **não há casos de teste UI automatizáveis para esta feature**. O
arquivo `test-cases/faturas-ui.md` lista apenas casos `[-]` que registram os
fluxos intencionados e os `data-testid` existentes, com a nota de tela
inacessível — para serem ativados quando (e se) a rota for adicionada.

### data-testids relevantes (InvoicesPage — inacessíveis)

`invoices-page`, `invoices-summary-{open,closed,paid}`, `invoices-list-card`,
`invoices-list-empty`, `invoices-list-item-{id}`, `invoices-list-item-{id}-close-btn`,
`invoices-detail-modal`, `invoices-detail-modal-content`, `invoices-detail-title`,
`invoices-detail-{due,total,paid}`, `invoices-detail-installments`,
`invoices-detail-installments-empty`, `invoices-detail-installment-{id}`,
`invoices-detail-pay-section`, `invoices-detail-pay-account-select`,
`invoices-detail-pay-btn`, `invoices-detail-close-btn`.

---

## Regras de negócio (UI)

1. **A tela de faturas não existe na navegação** (`InvoicesPage` não roteada) —
   fluxo de fatura é alcançável apenas por API ou indiretamente por
   dashboard/planejamento.
2. **Dashboard reflete fatura** via `dashboard-next-invoice-card` (primeira
   fatura `OPEN`).
3. **Planejamento reflete faturas** via `planning-invoice-{id}` (faturas
   `OPEN` do mês).
4. **Sem ações de fatura na UI** — fechar/pagar são operações somente-API até a
   rota de faturas existir.

---

## Critérios de aceite (UI)

Nenhum automatizável nesta feature — a única entrega de UI é a **não-navegável**
(currente): não existe caso feliz de fatura no front. Os casos `[-]` em
`faturas-ui.md` documentam o comportamento intencionado para quando a rota
existir (listar, fechar, detalhar, pagar), todos com a restrição de tela
inacessível.

---

## Referência (código-fonte — app finance-control)

- `backend/src/main/java/br/com/financecontrol/controller/InvoiceController.java`
- `backend/src/main/java/br/com/financecontrol/service/InvoiceService.java`
- `backend/src/main/java/br/com/financecontrol/dto/invoice/InvoiceResponse.java` / `InvoiceDetailResponse.java` / `InvoiceInstallmentResponse.java` / `PayInvoiceRequest.java`
- `backend/src/main/java/br/com/financecontrol/entity/Invoice.java` / `entity/enums/InvoiceStatus.java`
- `backend/src/main/resources/messages.properties` (`invoice.*`, `account.*`)
- `frontend/src/pages/InvoicesPage.tsx` (não roteada)
- `frontend/src/routes/AppRoutes.tsx` (sem import de InvoicesPage)