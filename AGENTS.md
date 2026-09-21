# AGENTS.md — Convenções do finance-control-tests

Guia de convenções para estender esta suíte. Mantenha-o atualizado.

## Regra de ouro

`src/test/java` contém **somente classes de teste** (métodos `@Test`). Toda infraestrutura vive em `src/main/java`.

```
TESTE (o quê)  →  PageObject/APIClient (como)  →  infra (driver/config/http)
```

## Como criar um novo teste de API

1. `api/requests/XxxRequest.java` — record Jackson do payload.
2. `api/models/XxxResponse.java` — record Jackson da resposta.
3. `api/clients/XxxClient.java` — estende `ClientBase`; um método por operação HTTP, retornando `Response` cru.
4. `builders/XxxBuilder.java` — fluent, dados default aleatórios via `UserFaker` ou faker próprio.
5. `fixtures/XxxFixture.java` — cria estado pré-condição via API (idempotente, dados únicos).
6. `builders`/`fixtures` **nunca** contêm asserts de negócio — só falham em pré-condição.
7. `tests/api/XxxApiTest.java` — `@Tag("api")` na classe, `@Tag("smoke")` no happy path. Methods `should...` (AAA).
8. Contrato (JSON Schema) é `@Tag("contract")` em método **dentro da própria** classe de API — nunca classe/diretório `contract` separado.

## Como criar um novo teste web

1. Inspecionar o app e usar os `data-testid` existentes (não XPath frágil).
2. `pages/XxxPage.java` — estende `BasePage`; locators `By` privados; métodos fluent orientados a comportamento; waits explícitos, nunca `Thread.sleep`.
3. Componentes reutilizados → `pages/components/`.
4. `tests/web/XxxWebTest.java` — `@Tag("web")`. Estado criado por API via fixture (login via `TestUserFixture`).

## Padrões

- Nomes: `shouldLoginSuccessfully`, `shouldRejectInvalidPassword`. Nada de `test1`.
- AAA: Arrange (fixture/builder) → Act (client/page) → Assert (assertj/junit).
- Massa: sempre dados dinâmicos/únicos (`UserFaker`), nunca valores fixos em testes.
- Tags: `api|web|contract|smoke|e2e`. Profiles Maven: `-Psmoke/-Papi/-Pweb/-Pcontract/-Pe2e`.
- Logging: SLF4J (`LoggerFactory`). Nunca `System.out.println`.
- Secrets: não existem reais. `.env` gitignored; default seguro no `ConfigManager`; variáveis de ambiente têm precedência.
- Evitar paralelismo por enquanto; nada de estado global mutável. `DriverManager` usa `ThreadLocal` para isolar o driver.

## Commands

```bash
mvn clean test                 # tudo
mvn clean test -Papi -Psmoke   # etc.
mvn surefire-report:report     # html em target/site
docker compose -f ../finance-control/docker-compose.yml up -d   # sobe o app
```