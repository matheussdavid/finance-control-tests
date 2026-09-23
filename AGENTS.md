# AGENTS.md — Convenções do finance-control-tests

Guia de convenções para estender esta suíte. Mantenha-o atualizado.

## Regra de ouro

Todo o código da suíte vive em `src/test/java` (não existe `src/main` — é um projeto só de automação). `tests/**` contém **somente** classes de teste (métodos `@Test`); PageObject, API Client, Fixture, Builder, Repository, driver e config vivem fora de `tests/**`.

```
TESTE (o quê)  →  PageObject/APIClient (como)  →  infra (driver/config/http/db)
```

## Como criar um novo teste de API

1. `api/requests/XxxRequest.java` — record Jackson do payload.
2. `api/models/XxxResponse.java` — record Jackson da resposta.
3. `api/clients/XxxClient.java` — estende `ClientBase`; um método por operação HTTP, retornando `Response` cru.
4. `builders/XxxBuilder.java` — fluent, dados default aleatórios via `UserFaker` ou faker próprio.
5. `fixtures/XxxFixture.java` — cria estado pré-condição via API (idempotente, dados únicos).
6. `builders`/`fixtures` **nunca** contêm asserts de negócio — só falham em pré-condição.
7. `tests/api/XxxApiTest.java` — `@Tag("api")` na classe, `@Tag("smoke")` no happy path. Métodos `deve...` (pt-BR sem acento, verbo no presente), em ordem: **contrato → fluxo principal → demais cenários**. `@DisplayName` descritivo, **sem** códigos/IDs (ex.: CT-001) — casos entram/saem sem renumeração. AAA (Arrange → Act → Assert).
8. Contrato (JSON Schema) é `@Tag("contract")` em método **dentro da própria** classe de API — nunca classe/diretório `contract` separado. Schema em `src/test/resources/schemas`.

## Como criar um novo teste web

1. Inspecionar o app e usar os `data-testid` existentes (não XPath frágil).
2. `web/pages/XxxPage.java` — estende `BasePage`; locators `By` privados; métodos fluent orientados a comportamento; waits explícitos, nunca `Thread.sleep`.
3. Componentes reutilizados → `web/components/`.
4. `tests/web/XxxWebTest.java` — `@Tag("web")`. Estado criado por API via fixture (login via `TestUserFixture`).

## Como estender o acesso ao banco

1. `database/XxxRepository.java` — um método de consulta (PreparedStatement + record de saída). Sem ORM.
2. Suporte a novo fluxo persistido: API cria o registro (client/fixture) → repository consulta → teste asserta.
3. `database/` **não** contém asserts — só retorna os dados consultados.

## Documentação por feature (docs/)

A ordem de automação e os casos de teste por feature vivem em `docs/`:

- `docs/plano-automacao.md` — guia geral: ordem por dependência, prioridades P0–P3, status por feature e limitações do app.
- `docs/features/<feature>/<feature>.md` — requisitos (descrição, endpoints, regras de negócio API+UI, critérios de aceite).
- `docs/features/<feature>/test-cases/<feature>-api.md` / `-ui.md` / `-revisao.md` — casos de teste gerados e revisados pelas skills.

Fluxo por feature:
1. Escrever `<feature>.md` (requisitos) a partir do código-fonte do app.
2. Rodar a skill `test-case-generator` → gera `-api.md` e `-ui.md` (bloco de contratos P0 abre o arquivo API).
3. Rodar a skill `test-case-reviewer` → deduplica, valida cobertura e gera `-revisao.md`; deixa os arquivos já revisados.
4. Automatizar os casos em Java; marcar Status `[x]` (automatizado), `[ ]` (planejado) ou `[-]` (não automatizável — limitação do app).

Skills ficam em `.opencode/skills/` (**gitignored**, locais — não versionar). Os CTs internos dos docs usam `CT-00X`, mas o `@DisplayName` dos testes Java é descritivo, sem IDs.

## Padrões

- Nomes: `deveAutenticarUsuarioComCredenciaisValidas` (pt-BR sem acento, presente). Nada de `test1`.
- AAA: Arrange (fixture/builder) → Act (client/page) → Assert (assertj/junit).
- Massa: sempre dados dinâmicos/únicos (`UserFaker`), nunca valores fixos em testes.
- Tags: `api|web|contract|smoke`. Execução isolada por grupo com `-Dgroups` (ex.: `-Dgroups=api`). Só crie um novo grupo se a execução separada for real.
- Criar abstração apenas se houver duplicação real e leitura melhor: 10 linhas simples > 3 linhas com 5 abstrações.
- Logging: SLF4J (`LoggerFactory`). Nunca `System.out.println`.
- Secrets: não existem reais. `.env` gitignored; default seguro no `ConfigManager`; variáveis de ambiente têm precedência.
- Sem paralelismo por enquanto; nada de estado global mutável.

## Commands

```bash
mvn clean test                 # tudo (api + contract + db + web)
mvn clean test -Dgroups=api    # api (+ contract + db)
mvn clean test -Dgroups=web    # web (Selenium)
mvn clean test -Dgroups=contract
mvn clean test -Dgroups=smoke
mvn surefire-report:report     # html em target/site
docker compose -f ../finance-control/docker-compose.yml up -d   # sobe o app
```