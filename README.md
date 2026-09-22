# finance-control-tests

Automação de testes para a aplicação [finance-control](https://github.com/matheussdavid/finance-control) (gerenciador financeiro pessoal — Spring Boot + React + PostgreSQL + JWT).

## 1. Objetivo

Demonstrar uma suíte de testes automatizados simples, clara e profissional, usada por quem está aprendendo automação (QA Jr/Pleno). O projeto cobre:

- testes de **API** com Rest Assured;
- testes **Web** com Selenium + Page Object Model;
- testes de **contrato** (JSON Schema);
- **validação de persistência** consultando o PostgreSQL via JDBC;
- massa de teste dinâmica com **Faker** e **Fixtures** (estado criado via API);
- execução isolada por grupo e em **CI** (GitHub Actions);
- evidências de falha (screenshot + diagnóstico).

Arquitetura em uma linha:

> **TESTE (o quê) → PageObject/APIClient (como) → infra (driver/config/http/db)**

## 2. Tecnologias

| Tecnologia | Objetivo |
|---|---|
| Java 21 | Linguagem |
| JUnit 5 | Test runner |
| Selenium 4 | Testes Web |
| Rest Assured | Testes de API |
| JSON Schema (Rest Assured) | Testes de contrato |
| JDBC | Validação de dados no banco |
| PostgreSQL | Banco da aplicação sob teste |
| DataFaker | Massa de teste dinâmica |
| AssertJ | Asserções |
| JUnit `@Tag` / `-Dgroups` | Execução isolada por tipo |
| Maven | Build |
| Docker Compose | Ambiente da aplicação sob teste |
| GitHub Actions | CI |

## 3. Arquitetura

Este repositório contém **somente automação** — todo o código vive em `src/test`:

```
src/test
├── java/br/com/financecontrol
│   ├── api/            # clients (HTTP), models (respostas), requests (payloads)
│   ├── builders/       # construção de massa (TestUserBuilder)
│   ├── config/         # ConfigManager (env > .env > default)
│   ├── core/           # TestBase, WebTestBase, WebTestWatcher
│   ├── data/           # TestUser + UserFaker (dados dinâmicos)
│   ├── database/       # JDBC: DatabaseConnection + repositories
│   ├── driver/         # DriverFactory (Chrome, headless)
│   ├── fixtures/       # TestUserFixture, FinanceFixture (pré-condição via API)
│   ├── tests/          # SOMENTE classes/métodos de teste
│   │   ├── api/        # LoginApiTest, TransactionPersistenceTest
│   │   └── web/        # LoginWebTest
│   └── web/pages/      # Page Objects (LoginPage, DashboardPage, BasePage)
└── resources
    ├── schemas/        # contratos JSON Schema
    ├── logback.xml     # logging (SLF4J)
    └── junit-platform.properties
```

Regras simples:

- infraestrutura e Page Objects ficam em `src/test/java` (não existe `src/main` — não há código de produção);
- `tests/**` contém **somente** métodos `@Test` — nada de driver/locator/client dentro deles;
- contratos JSON Schema ficam em `src/test/resources/schemas` e são validados **dentro** da classe de API (`@Tag("contract")`);
- sem abstrações desnecessárias: client por endpoint, fixture por pré-condição, page por tela usada.

## 4. Estratégia de testes

| Tipo | O que valida | Tags |
|---|---|---|
| API | `POST /auth/login` com credenciais válidas → status + token + usuário | `api`, `smoke` |
| Contract | Resposta de login respeita o JSON Schema em `schemas/auth/login-response.json` | `api`, `contract`, `smoke` |
| Web | Login via UI → dashboard é exibido | `web`, `smoke` |
| Database | Despesa criada via API → conferida no PostgreSQL (valor, tipo, data, conta, categoria) | `api` |

Massa e fixtures: `TestUserFixture` registra um usuário **único** via API (Faker) e devolve o `TestUser` com id/token; `FinanceFixture` cria conta + categoria para cenários financeiros. Os testes **não** repetem a preparação.

## 5. Como executar localmente

Pré-requisitos: Java 21, Maven 3.9+, Docker, Chrome.

Suba a aplicação sob teste:

```bash
cd ../finance-control          # repo do app
cp .env.example .env           # preencha JWT_SECRET (>= 32 caracteres)
docker compose up --build -d
```

Frontend: `http://localhost:5173` · API: `http://localhost:8080`.

Rode a suíte inteira (api + contract + db + web):

```bash
mvn clean test
```

## 6. Como executar por tipo

| Grupo | Comando | O que roda |
|---|---|---|
| Tudo | `mvn clean test` | api + contract + db + web |
| API (+ contract + db) | `mvn clean test -Dgroups=api` | `@Tag("api")` |
| Contract | `mvn clean test -Dgroups=contract` | `@Tag("contract")` |
| Web | `mvn clean test -Dgroups=web` | `@Tag("web")` (Selenium) |
| Smoke | `mvn clean test -Dgroups=smoke` | `@Tag("smoke")` — caminhos felizes |

Teste único:

```bash
mvn test -Dtest=LoginApiTest
mvn test -Dtest="LoginApiTest#shouldLoginWithValidUsername"
```

Web headless: definido por `HEADLESS=true` (padrão). Em CI nunca muda.

## 7. Configuração

Precedência: **variável de ambiente → `.env` → padrão no código**. Copie `.env.example` para `.env` se quiser ajustar (`.env` é gitignored).

| Chave | Default | Descrição |
|---|---|---|
| `BASE_URL` | `http://localhost:5173` | URL do frontend |
| `API_BASE_URL` | `http://localhost:8080` | URL da API |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/finance_control` / `finance` / `finance_pass` | Banco (validação de persistência) |
| `HEADLESS` | `true` | Chrome headless |
| `CHROME_BINARY` | *(vazio)* | Binário do Chrome fora do local padrão |
| `WAIT_TIMEOUT_SECONDS` | `15` | Timeout dos waits explícitos |
| `SCREENSHOT_DIR` | `target/screenshots` | Evidências de falha visual |
| `DEFAULT_PASSWORD` | `Passw0rd!123` | Senha sintética dos usuários criados em teste |

Não existem secrets reais: usuários e tokens são criados dinamicamente a cada teste.

## 8. CI

`.github/workflows/tests.yml`:

1. **Job `api` (API + Contract + DB)** — checkout dos dois repositórios (testes + app) → JDK 21 (cache Maven) → `docker compose up --build` → aguarda a API responder → `mvn clean test -Dgroups=api` → upload de reports.
2. **Job `web`** (após `api`) — mesmo setup → `mvn clean test -Dgroups=web` → upload de reports + evidências (`target/screenshots`) → `docker compose down -v`.

Executa em `push`/`PR` para `main` e manualmente (`workflow_dispatch`).

> Se o repositório do app for privado, crie um PAT e configure o secret `APP_REPO_TOKEN` (o app é ajustável no step "Checkout (app repo)").

## 9. Evidências e relatórios

- Em falha de teste Web, `WebTestWatcher` salva **screenshot + URL + título** em `target/screenshots/` (vira artefato no CI).
- Resultados por execução: `target/surefire-reports/*.txt|xml`.
- Relatório HTML: `mvn surefire-report:report` → `target/site/surefire-report.html`.
- Logs: `target/logs/test-execution.log` (sem senhas/tokens).

## 10. Test coverage

Aplicação sob teste (Finance Control) — fluxos automatizados neste repositório:

| Fluxo | Tipo | Teste |
|---|---|---|
| Login com credenciais válidas (via API) | API | `LoginApiTest#shouldLoginWithValidUsername` |
| Formato da resposta de login | Contract | `LoginApiTest#shouldMatchLoginResponseSchema` |
| Login pela interface → dashboard | Web | `LoginWebTest#shouldLoginSuccessfully` |
| Despesa via API → confirmada no PostgreSQL | Database | `TransactionPersistenceTest#shouldPersistExpenseCreatedViaApi` |

**1 exemplo funcional por tipo**, intencionalmente pequeno para servir de molde a novos fluxos.