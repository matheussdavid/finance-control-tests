# Mudanças — CT-013 (Buscar conta por id)

**Branch:** `feat/testes-contas`
**Data:** 2026-09-24
**Projeto:** finance-control-tests (testes de API de contas)

---

## Objetivo

Automatizar o **CT-013** de `docs/features/contas/test-cases/contas-api.md`:

> *Buscar conta por id retorna a conta* (`GET /accounts/{id}` → 200 com os campos da conta).

Estratégia aplicada (alinhada com a conversa de revisão):

- **Massa de consulta** vem de um **usuário-catálogo** (seed) criado **antes** dos testes;
- o teste **não cria nada**: loga no catálogo, acha a conta no banco por `user_id` e consulta via API;
- **demais verbos** (POST/PUT/PATCH futurоs) seguirão com **massa própria** por teste.

---

## Fluxo do teste (linha a linha)

```
SeedFixture.seedContas()  (@BeforeAll — roda antes dos testes da classe)
│
├─ sessionUser()
│    ├─ POST /auth/login  (email/senha fixos do catálogo) → 200? reusa usuário
│    └─ 401 → POST /auth/register → 201 usa | 409 → falha clara (senha divergiu)
│
├─ ensureAccountsOfEveryType()
│    ├─ AccountRepository.findActiveTypesByUserId(userId)
│    │    └─ SQL: SELECT DISTINCT type ... WHERE user_id = ? AND status='ACTIVE'
│    └─ criar via POST /accounts só os tipos ausentes (saldos 0.00)
│
└─ devolve TestUser (com token)

GetAccountTest
├─ AccountRepository.findByUserId(userId)  → SQL: WHERE user_id = ? ... LIMIT 1
│    └─ (oracle da massa pré-criada — não é o teste que gravou)
├─ GET /accounts/{id} com o token do catálogo → 200
└─ asserts: id/name/type/initialBalance/balance/status
```

**Por que o banco nesta consulta?** A conta vem de uma massa **pré-existente** (criada no seed,
não por este teste). Comparar o body com a linha do banco valida que a API devolveu o que está
persistido — é fonte de descoberta, **não** echo test (a crítica da análise anterior).

---

## Arquivos

### 1. `config/ConfigManager.java` — adicionadas 2 constantes
- `SEEDED_ACCOUNT_EMAIL` (default `seed.contas@qa.example.com`)
- `SEEDED_ACCOUNT_PASSWORD` (default `SeedContas!123`)

Credenciais **determinísticas** do catálogo, com **senha própria** (não herdam `DEFAULT_PASSWORD`),
para que mudança de `.env` não órfane o usuário já criado no banco.

### 2. `.env.example` — documentadas as 2 chaves novas
Visíveis para quem configurar o ambiente em outro lugar (CI/dev).

### 3. `database/AccountRepository.java` — `findAny()` → `findByUserId(String userId)` + `findActiveTypesByUserId(String userId)`
- `findByUserId`: query escopada por `user_id` (como o `TransactionRepository` faz);
- resolve o bug da versão anterior: `findAny()` devolvia conta de **qualquer** usuário, e a fixture
  não tinha como autenticar o dono → o `GET /accounts/{id}` daria **404 de ownership** no app.
- `findActiveTypesByUserId`: `SELECT DISTINCT type ... WHERE user_id = ? AND status = 'ACTIVE'`
  — usado pelo seed para descobrir **quais tipos já existem** e criar só o que falta.

### 4. `api/models/AccountResponse.java` — timestamps como `String`
- `LocalDateTime createdAt` → `String createdAt` + adicionado `String updatedAt`;
- motivo: o `pom.xml` **não** tem `jackson-datatype-jsr310`, e `extract().as(AccountResponse.class)`
  lançaria `InvalidDefinitionException` com `LocalDateTime`. O app envia ISO string.

### 5. `fixtures/SeedFixture.java` — **novo** (arquivo único de seeds)
- Contém `seedContas()` — a massa de consulta de contas;
- estrutura pensada para agrupar **seeds de outras funções no mesmo arquivo**
  (`seedTransacoes()`, `seedCartoes()`, ...) — um arquivo, um método por função;
- idempotente: reusa usuário/contas já existentes, cria apenas o que falta;
- **simplificado** para legibilidade/portfólio: retorna só `TestUser` (sem wrapper `ContaSeed`),
  sem `TypeRef`/`Map<String,String>`/`Function` — quem diz o que existe é o **banco**
  (`findActiveTypesByUserId`), a API só cria o tipo ausente;
- contas com nomes fixos (`Seed CHECKING|SAVINGS|CASH`) e **saldo 0.00** (saldo não é massa de consulta);
- auto-cura: só cria tipo que **não tem conta ACTIVE**.

### 6. `fixtures/AccountsFixture.java` — limpeza
- Removido o `findOrCreateAccount()` **global** (não compilava e era o bug de ownership);
- mantido `createAccountContext()` (usuário fresco + `POST /accounts`) — será usado pela
  **massa própria** dos verbos de mutação (POST/PUT/PATCH);

### 7. `tests/api/accounts/GetAccountTest.java` — reescrito para o CT-013
- `@BeforeAll` monta o seed (uma vez por classe — `per_class` já configurado);
- `accountClient` agora é criado **com o token do catálogo** (antes ficava `null` → NPE);
- asserts sem `floatValue()` (trap Double/Float): `isEqualByComparingTo` para `BigDecimal`;
- com o `SeedFixture` simplificado, guarda `TestUser` direto (`seed.userId()` / `seed.token()`).

### 8. `docs/features/contas/test-cases/contas-api.md` — CT-013 marcado `[x]`

---

## Verificação executada

| Comando | Resultado |
|---|---|
| `mvn -q test-compile` | Compila sem erros |
| `mvn test -Dgroups=api -Dtest=GetAccountTest` (1ª execução) | **1 test, 0 failures** |
| `mvn test -Dgroups=api -Dtest=GetAccountTest` (2ª execução) | **1 test, 0 failures** (idempotência) |
| `mvn clean test -Dgroups=api -Dtest=GetAccountTest` (pós-refatoração) | **1 test, 0 failures** (build limpo) |
| `SELECT ... WHERE email = 'seed.contas@qa.example.com'` | **1 usuário · 3 contas** `CASH/CHECKING/SAVINGS` todas `ACTIVE` |

A 2ª execução provou a idempotência: o seed **não duplicou** usuário nem contas.

---

## Como rodar

```bash
# app + banco no ar
docker compose -f ../finance-control/docker-compose.yml up -d

# só o CT-013
mvn clean test -Dgroups=api -Dtest=GetAccountTest

# ou todo o grupo de API
mvn clean test -Dgroups=api
```

---

## Próximos passos sugeridos (fora deste escopo)

- `ListAccountsTest` (CT-010) e contratos de consulta (CT-008/011) reaproveitando `SeedFixture.seedContas()`;
- seeds de outras funções entrando no mesmo `SeedFixture`;
- massa própria nos verbos POST/PUT/PATCH via `AccountsFixture`;
- atualizar `AGENTS.md` com a convenção: *seed único por função + consulta usa catálogo, mutação usa massa própria*.