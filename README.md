# finance-control-tests

Automação de testes **E2E** para a aplicação [finance-control](https://github.com/matheussdavid/finance-control) (gerenciador financeiro pessoal — Spring Boot + React + PostgreSQL + JWT).

## Objetivo

Demonstrar uma suíte de automação profissional com separação clara de responsabilidades:

> **A camada de testes descreve o comportamento validado. A infraestrutura que executa o teste (WebDriver, HTTP, JSON, massa, waits, configuração) vive fora dela.**

```
TESTE (o quê)  →  PageObject/APIClient (como)  →  infra (driver/config/http)
```

Neste repositório você encontra **a infraestrutura completa** e **dois testes-guia de login** (web e api — este último com os testes de contrato JSON Schema no mesmo arquivo) que servem de molde para criar novos testes (veja [AGENTS.md](AGENTS.md)).

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 (LTS) |
| Build | Maven |
| Testes | JUnit 5 (aggregator), AssertJ |
| API | REST Assured + JSON Schema (contract) |
| JSON | Jackson (databind) |
| Web | Selenium 4 (Selenium Manager resolve o driver sozinho) |
| Dados | DataFaker (massa dinâmica/única) |
| Config | dotenv (`env var > .env > default`) |
| Logs | SLF4J + Logback |
| CI | GitHub Actions |

## Arquitetura

```
src/main/java  →  infraestrutura (config, driver, pages, api clients, builders, fixtures, utils)
src/test/java   →  SOMENTE classes de teste (web, api — contrato é tag dentro do teste de API)
src/test/resources/schemas  →  contratos JSON Schema (única exceção que mora no test layer)
```

Camadas principais:

- **config** — `ConfigManager`: leitura centralizada de `BASE_URL`, `API_BASE_URL`, `BROWSER`, `HEADLESS`, timeouts.
- **core** — bases de teste (`TestBase`, `WebTestBase`), inicialização do Rest Assured e watcher de evidências.
- **driver** — `Browser` (enum), `DriverFactory` (options headless/CI-safe) e `DriverManager` (`ThreadLocal` — pronto para paralelismo futuro).
- **pages** — `BasePage` + Page Objects (`LoginPage`, `DashboardPage`). Locators encapsulados, métodos de comportamento, waits explícitos, **nunca** `Thread.sleep`.
- **api** — `ClientBase` + `AuthClient` (um método por operação HTTP), `requests` (DTOs de entrada), `models` (DTOs de saída). O teste nunca faz `given()...post()`.
- **builders/fixtures/data** — massa: `TestUserBuilder`, `TestUserFixture` (registra usuário único via API), `UserFaker` (dados dinâmicos).
- **utils** — `ScreenshotUtil` (evidência de falha: screenshot + URL + título).

## Estrutura de diretórios

```text
.
├── .github/workflows/tests.yml        # CI: jobs api (incl. contract) e web
├── .env.example                       # modelo de configuração (committed)
├── .env                               # config local (gitignored)
├── AGENTS.md                          # convenções para estender a suíte
├── src
│   ├── main/java/br/com/financecontrol
│   │   ├── api/{clients, models, requests}
│   │   ├── builders
│   │   ├── config
│   │   ├── core
│   │   ├── data
│   │   ├── driver
│   │   ├── fixtures
│   │   ├── pages/{components}
│   │   └── utils
│   ├── main/resources/logback.xml
│   └── test
│       ├── java/br/com/financecontrol/tests/{api, web}
│       └── resources
│           ├── junit-platform.properties
│           └── schemas/{auth, common}
└── pom.xml
```

## Pré-requisitos

- **Java 21** (LTS) — se tiver JDK mais novo, configure com `maven.compiler.release=21`.
- **Maven 3.9+**
- **Docker** — para subir a aplicação sob teste (`finance-control`).
- **Chrome** — para testes web. Local: Chrome instalado no sistema OU qualquer binário, apontado por `CHROME_BINARY` (veja config). No CI o runner já tem Chrome.

### Subir a aplicação

```bash
cd ../finance-control          # repo do app
cp .env.example .env           # preencha JWT_SECRET (>= 32 caracteres)
docker compose up --build -d
```

- Frontend: `http://localhost:5173`
- API: `http://localhost:8080` (Swagger: `/swagger-ui.html`)

O frontend recebeu um `data-testid` extra (`message-error`) para viabilizar a asserção de erro de login — se você alterar o app, recompile a imagem (`docker compose up --build`).

## Configuração

Precedência: **variável de ambiente → `.env` → padrão no código**. Copie `.env.example` para `.env` e ajuste:

| Chave | Default | Descrição |
|---|---|---|
| `BASE_URL` | `http://localhost:5173` | URL do frontend |
| `API_BASE_URL` | `http://localhost:8080` | URL da API |
| `BROWSER` | `chrome` | `chrome` / `firefox` / `edge` (estrutura pronta p/ os 2 últimos) |
| `HEADLESS` | `true` | headless no CI |
| `CHROME_BINARY` | *(vazio)* | caminho do binário Chrome fora do local padrão |
| `WAIT_TIMEOUT_SECONDS` | `15` | timeout dos waits explícitos |
| `DEFAULT_PASSWORD` | `Passw0rd!123` | senha sintética dos usuários de teste (não é secret real) |

Não há secrets reais neste projeto — os usuários/tokens são criados dinamicamente por cada teste. `.env` é gitignored.

## Execução local

```bash
mvn clean test                # tudo (api + contract + web)
```

## Execução por tipo (profiles = tags JUnit)

| Grupo | Comando | O que roda |
|---|---|---|
| Tudo | `mvn clean test` | todos os testes |
| Smoke | `mvn clean test -Psmoke` | `@Tag("smoke")` — caminhos felizes |
| API | `mvn clean test -Papi` | `@Tag("api")` + `@Tag("contract")` |
| Web | `mvn clean test -Pweb` | `@Tag("web")` (Selenium) |
| Contract | `mvn clean test -Pcontract` | `@Tag("contract")` — métodos de JSON Schema dentro de `LoginApiTest` |
| E2E | `mvn clean test -Pe2e` | `@Tag("e2e")` — fluxos web+api |

Teste único:

```bash
mvn test -Dtest=LoginApiTest
mvn test -Dtest="LoginApiTest#shouldRejectInvalidPassword"
```

## GitHub Actions

`.github/workflows/tests.yml`:

1. **Job `api`** — checkout dos dois repositórios (testes + app `finance-control`) → JDK 21 (cache maven) → `docker compose up --build` → aguarda API → `mvn clean test -Papi` → upload de reports.
2. **Job `web`** (após `api`) — mesmo setup → `mvn clean test -Pweb` → upload de reports + **evidências** (`target/screenshots`) → `docker compose down -v`.

Executa em `push`/`PR` para `main` e manualmente (`workflow_dispatch`).

> Se o repositório do app for **privado**, crie um PAT com acesso a ele e configure o secret `APP_REPO_TOKEN` neste repo. A URL do app é ajustável no `repository:` do step "Checkout (app repo)".

## Relatórios

- **Por execução** (definitivo): `target/surefire-reports/*.txt|xml` — resultados, duração, stack trace.
- **HTML**: `mvn surefire-report:report` → `target/site/surefire-report.html`.
- **Evidências web**: em falha, `WebTestWatcher` salva screenshot + URL + título em `target/screenshots/` (anexado ao CI).
- **Logs**: `target/logs/test-execution.log` (sem senhas/tokens).

## Exemplos guia (leia para aprender)

1. **API** — `tests/api/LoginApiTest.java`: `fixture` cria usuário, `client` faz a chamada, teste só monta Arrange/Act/Assert. Os métodos `@Tag("contract")` validam JSON Schema (`src/test/resources/schemas/`) no mesmo arquivo.
2. **Web** — `tests/web/LoginWebTest.java`: estado criado por API, interação nos Page Objects, teste não conhece locator/driver.

Para criar o próximo recurso (ex.: contas), siga o passo a passo em [AGENTS.md](AGENTS.md).