# Reconstrução do projeto de automação — Finance Control Tests
Você deve reconstruir **do zero** o projeto de automação deste repositório.

O projeto atual foi estruturado de forma excessivamente complexa e não deve ser preservado apenas por compatibilidade. **Você pode apagar, mover, renomear e recriar arquivos e diretórios livremente.**

O objetivo desta reconstrução não é criar um framework de automação sofisticado ou "enterprise".

O objetivo é criar uma **suíte de testes automatizados simples, clara, profissional e fácil de entender**, adequada para um portfólio de um QA Jr/Pleno que está desenvolvendo experiência prática com:

- Selenium
- Java
- JUnit 5
- Rest Assured
- Page Object Model
- testes de API
- testes de contrato
- acesso ao banco de dados
- validação de persistência
- massa de teste
- dados dinâmicos com Faker
- fixtures
- testes E2E
- Docker
- GitHub Actions

---

# 1. Princípio principal
A arquitetura deve ser consequência das necessidades dos testes.

**Não crie abstrações antecipadamente apenas porque elas são consideradas boas práticas.**

Não tente criar:

- framework genérico;
- arquitetura enterprise;
- excesso de interfaces;
- excesso de factories;
- excesso de design patterns;
- abstrações para uso futuro;
- suporte a recursos que não são utilizados;
- paralelismo sem necessidade;
- múltiplos browsers apenas para demonstrar conhecimento;
- Selenium Grid;
- infraestrutura complexa;
- dezenas de classes Base;
- utilitários genéricos sem necessidade real.
A pergunta principal para qualquer abstração deve ser:

> "Existe uma necessidade real nos testes atuais que justifique essa abstração?"
Se não existir, não crie.

O código deve ser simples o suficiente para que um QA Jr/Pleno consiga explicar cada camada em uma entrevista.

---

# 2. Objetivo do projeto
O repositório deve demonstrar uma suíte de automação de uma aplicação financeira chamada **Finance Control**.

A aplicação sob teste já existe em outro repositório:

`finance-control`

Este repositório contém **somente a automação**.

Não altere a aplicação sob teste, a menos que isso seja estritamente necessário para descobrir como executar os testes.

O projeto de automação deve ser capaz de:

1. testar a API;
2. testar a interface Web;
3. validar contratos da API;
4. preparar massa através da API;
5. gerar dados dinâmicos;
6. validar dados persistidos no banco;
7. executar fluxos E2E;
8. rodar tudo em CI;
9. gerar evidências úteis quando houver falha.

---

# 3. Stack obrigatória
Utilize:

### Linguagem

- Java 21

### Build

- Maven

### Testes

- JUnit 5

### Assertions

- AssertJ

### Web

- Selenium WebDriver

### API

- Rest Assured

### Dados

- Java Faker

### JSON

- Jackson, somente quando realmente necessário

### Contrato

- JSON Schema utilizando suporte do Rest Assured

### Banco

- JDBC
- PostgreSQL

### CI

- GitHub Actions

### Ambiente

- Docker / Docker Compose quando necessário para subir a aplicação sob teste e suas dependências.
Não introduza outros frameworks sem necessidade.

---

# 4. Estrutura desejada
A estrutura deve ser simples e orientada ao propósito de automação.

Prefira:

```
src/
└── test/
    ├── java/
    │   └── br/
    │       └── com/
    │           └── financecontrol/
    │               ├── api/
    │               │   ├── clients/
    │               │   └── models/
    │               │
    │               ├── web/
    │               │   ├── pages/
    │               │   └── components/
    │               │
    │               ├── database/
    │               │
    │               ├── fixtures/
    │               │
    │               ├── data/
    │               │
    │               └── tests/
    │                   ├── api/
    │                   ├── web/
    │                   └── e2e/
    │
    └── resources/
        └── schemas/
```
Essa estrutura é uma sugestão e pode ser ajustada caso exista uma razão concreta.

Não crie dezenas de subdiretórios apenas para deixar a estrutura "bonita".

---

# 5. Regra importante sobre src/main
Não coloque Page Objects, API Clients, Fixtures, Repositories ou classes de suporte em `src/main/java` apenas porque são "código".

Este é um projeto exclusivamente de automação.

Esses componentes existem para os testes.

Portanto, prefira mantê-los em:

```
src/test/java
```
Não crie `src/main/java` se não houver código de produção real.

---

# 6. Testes Web — Selenium + POM
Os testes Web devem utilizar **Page Object Model**.

O teste não deve manipular diretamente `WebDriver` ou locators sempre que isso puder ser encapsulado pelo Page Object.

Exemplo conceitual:

```
@Test
void shouldLoginSuccessfully() {
    loginPage.loginAs(user);

    dashboardPage.shouldBeDisplayed();
}
```
O teste deve expressar comportamento.

Evite:

```
driver.findElement(...).click();
driver.findElement(...).sendKeys(...);
```
diretamente dentro dos testes.

---

# 7. Page Objects
Crie Page Objects somente para páginas que realmente são utilizadas.

Inicialmente deve existir pelo menos:

```
LoginPage
DashboardPage
```
E posteriormente páginas necessárias para os fluxos financeiros.

O Page Object deve:

- armazenar locators;
- encapsular interação com a página;
- expor comportamentos;
- evitar detalhes de Selenium nos testes.
Evite criar uma hierarquia complexa de Page Objects.

---

# 8. Esperas Selenium
Utilize `WebDriverWait` e esperas explícitas.

Não utilize:

```
Thread.sleep(...)
```
como mecanismo normal de sincronização.

Não implemente loops manuais de polling se `WebDriverWait` resolver o problema.

Não crie múltiplos mecanismos de espera sem necessidade.

O objetivo é ter um mecanismo simples e consistente.

---

# 9. Seletores Selenium
Priorize seletores estáveis.

Ordem de preferência:

1. `data-testid` ou atributos equivalentes destinados a testes;
2. IDs estáveis;
3. atributos semânticos estáveis;
4. CSS;
5. XPath somente quando realmente necessário.
Evite XPath baseado em:

- posições;
- classes geradas dinamicamente;
- estruturas frágeis;
- textos que podem mudar sem impacto funcional.
Não altere a aplicação apenas para adicionar seletores, a menos que isso seja permitido e realmente necessário. Caso existam seletores de teste disponíveis, utilize-os.

---

# 10. Testes de API — Rest Assured
Os testes de API devem utilizar Rest Assured.

Inicialmente implemente pelo menos um caso de exemplo para:

### Sucesso

```
Login válido
→ status esperado
→ body esperado
```

### Negativo

```
Login com senha inválida
→ status esperado
→ mensagem esperada
```
A estrutura deve permitir posteriormente adicionar:

- cadastro;
- transações;
- receitas;
- despesas;
- categorias;
- dashboard;
- validações;
- autorização.

---

# 11. API Clients
Crie API Clients quando existir comportamento reutilizável.

Exemplo:

```
AuthClient
TransactionClient
CategoryClient
```
O Client deve ser responsável por executar chamadas HTTP.

Evite colocar assertions de negócio dentro do Client.

Preferencialmente:

```
Response response = authClient.login(user);
```
e o teste decide:

```
assertThat(response.statusCode()).isEqualTo(200);
```
O Client não deve saber se determinado status é esperado pelo teste.

---

# 12. Modelos / DTOs
Utilize DTOs quando eles realmente melhorarem a clareza.

Não crie DTOs para absolutamente todas as respostas se um teste simples puder validar o JSON diretamente.

Utilize objetos para representar entidades relevantes como:

```
User
Transaction
Category
```
quando isso reduzir duplicação e melhorar a legibilidade.

---

# 13. Massa de teste
O projeto deve demonstrar duas estratégias diferentes.

## Dados determinísticos
Utilize dados fixos quando o cenário exigir um valor específico.

Exemplo:

```
senha inválida
valor zero
campo obrigatório vazio
```

## Dados dinâmicos
Utilize Java Faker para dados que precisam ser únicos.

Exemplo:

```
faker.internet().emailAddress()
faker.name().fullName()
```
Não use Faker indiscriminadamente.

O dado deve ser dinâmico quando houver uma razão para isso.

---

# 14. Builders
Use Builder apenas quando ele realmente melhorar a criação dos objetos.

Por exemplo:

```
Transaction transaction = TransactionBuilder.valid()
    .withAmount(100)
    .withDescription("Lunch")
    .build();
```
Não crie Builders para objetos triviais que podem ser instanciados diretamente.

---

# 15. Fixtures
Crie Fixtures para preparar estados reutilizados pelos testes.

Exemplo:

```
UserFixture
```
pode:

1. gerar dados com Faker;
2. chamar a API de cadastro;
3. retornar o usuário criado.
O objetivo é permitir:

```
TestUser user = userFixture.create();
```
em vez de repetir todo o processo em cada teste.

Fixtures não devem esconder comportamentos importantes do cenário.

---

# 16. Banco de dados
O projeto deve demonstrar acesso ao PostgreSQL através de JDBC.

Crie uma camada simples para acesso ao banco.

Exemplo:

```
database/
├── DatabaseConnection
├── UserRepository
└── TransactionRepository
```
Não utilize ORM.

Não crie um framework de banco de dados.

O objetivo é apenas permitir que os testes consultem dados persistidos.

---

# 17. Validação de banco
Implemente pelo menos **um teste que valide a persistência no banco**.

Exemplo:

```
API
 ↓
cria transação
 ↓
HTTP 201
 ↓
consulta PostgreSQL
 ↓
confirma transação persistida
```
O teste deve validar dados relevantes, como:

- identificador;
- usuário;
- valor;
- tipo;
- descrição;
- data.
A consulta deve ser simples e legível.

Evite abstrações excessivas.

---

# 18. Teste Web + Banco
Se a aplicação permitir, implemente pelo menos um cenário onde:

```
Selenium
 ↓
executa ação na UI
 ↓
aplicação persiste informação
 ↓
PostgreSQL
 ↓
teste valida persistência
```
Se isso tornar o teste excessivamente acoplado à implementação interna da aplicação, não force o cenário.

O principal objetivo é demonstrar que o projeto sabe consultar o banco.

---

# 19. Testes de contrato
Utilize JSON Schema.

Implemente pelo menos:

```
Login API
→ response
→ JSON Schema
→ validação
```
Tenha pelo menos:

- um contrato de sucesso;
- um contrato de erro, caso exista schema apropriado.
Os schemas devem ficar em:

```
src/test/resources/schemas
```
Não coloque schemas dentro das classes de teste.

Não crie uma camada de abstração exclusiva para schemas sem necessidade.

---

# 20. Teste E2E
Implemente pelo menos **um fluxo E2E real** utilizando API + Web.

Exemplo:

```
API
 ↓
criar usuário
 ↓
Selenium
 ↓
login
 ↓
abrir transações
 ↓
criar despesa
 ↓
validar despesa na UI
```
Se fizer sentido para a aplicação, complemente:

```
UI
 ↓
ação
 ↓
API ou DB
 ↓
validação
```
Esse teste deve representar uma jornada real do usuário.

Não crie E2E artificial apenas para misturar tecnologias.

---

# 21. Separação entre API e Web
Os testes devem poder ser executados separadamente.

Exemplo:

```
mvn test -Dgroups=api
```
e:

```
mvn test -Dgroups=web
```
Se outra estratégia de execução com JUnit 5 for mais simples, utilize-a.

Não crie uma arquitetura de Maven Profiles complexa apenas para conseguir isso.

O mecanismo escolhido deve ser simples e documentado.

---

# 22. Categorias de teste
Utilize JUnit 5 `@Tag` quando isso melhorar a execução.

Sugestão:

```
api
web
contract
e2e
```
Não crie dezenas de tags.

---

# 23. CI — GitHub Actions
Crie uma pipeline funcional.

A pipeline deve:

1. fazer checkout do projeto de testes;
2. obter/subir a aplicação Finance Control;
3. subir as dependências necessárias;
4. preparar o banco;
5. executar os testes;
6. gerar evidências;
7. disponibilizar artefatos quando houver falha.
Estruture o pipeline de maneira simples.

Pode separar API e Web em jobs se isso melhorar a clareza.

Não crie uma pipeline excessivamente sofisticada.

---

# 24. Execução em CI
Os testes Selenium devem funcionar em ambiente headless.

O projeto deve permitir algo como:

```
CI=true
HEADLESS=true
```
ou uma configuração equivalente.

A execução local deve continuar sendo simples.

Exemplo:

```
mvn test
```

---

# 25. Evidências
Quando um teste Web falhar, capture pelo menos:

- screenshot;
- nome do teste;
- informações úteis para diagnóstico.
Os screenshots devem ser disponibilizados como artefatos do GitHub Actions.

Não implemente um sistema complexo de reporting.

---

# 26. Logs
Utilize logging somente onde ele realmente ajudar no diagnóstico.

Não adicione logging apenas para demonstrar conhecimento de SLF4J/Logback.

Evite:

```
System.out.println(...)
```
quando um log estruturado for necessário.

Mas também não crie dezenas de logs inúteis.

---

# 27. Configuração
A aplicação deve permitir configuração externa para pelo menos:

```
BASE_URL
API_URL
DB_URL
DB_USER
DB_PASSWORD
```
Utilize environment variables.

Valores default podem existir para facilitar a execução local, desde que não contenham credenciais reais.

Não coloque segredos reais no repositório.

---

# 28. Segurança
Nunca versionar:

- senhas reais;
- tokens reais;
- credenciais;
- arquivos `.env` contendo secrets reais.
Crie:

```
.env.example
```
se for necessário demonstrar configuração local.

No CI, utilize GitHub Actions Secrets quando houver segredo real.

---

# 29. O que NÃO implementar
Não implemente neste momento:

- Selenium Grid;
- execução paralela;
- ThreadLocal para WebDriver;
- suporte a vários browsers sem necessidade;
- Edge/Firefox apenas para aumentar a arquitetura;
- Cucumber;
- Gherkin;
- Serenity;
- Allure se não houver necessidade real;
- ReportPortal;
- arquitetura hexagonal;
- Clean Architecture;
- Dependency Injection framework;
- Spring;
- ORM;
- Repository genérico;
- Factory genérica;
- Abstract Factory;
- Strategy Pattern apenas para demonstrar pattern;
- Singleton para tudo;
- dezenas de classes `Base*`;
- wrappers genéricos para cada biblioteca;
- framework próprio de assertions;
- framework próprio de HTTP;
- framework próprio de Selenium.
**O projeto deve continuar pequeno o suficiente para ser entendido por uma única pessoa.**

---

# 30. Critério para criação de abstrações
Antes de criar uma nova classe, pergunte:

### Existe duplicação?
Se não:

> talvez não precise de abstração.

### Existe responsabilidade claramente diferente?
Se não:

> talvez não precise de outra classe.

### A abstração melhora a leitura do teste?
Se não:

> provavelmente não deve existir.

### A abstração é utilizada por mais de um teste ou fluxo?
Se não:

> considere manter o código local até surgir necessidade real.

---

# 31. Casos mínimos obrigatórios
Ao finalizar a reconstrução, o projeto deve conter pelo menos:

## Web

### Caso 1

```
Login válido
→ login
→ dashboard exibido
```

### Caso 2

```
Login inválido
→ mensagem de erro
```

---

## API

### Caso 1

```
Login válido
→ status esperado
→ body esperado
```

### Caso 2

```
Login inválido
→ status esperado
→ erro esperado
```

---

## Contract

### Caso 1

```
Login válido
→ JSON Schema
```

### Caso 2

```
Login inválido
→ JSON Schema
```
se houver schema apropriado.

---

## Database

### Caso 1

```
API cria registro
→ consulta PostgreSQL
→ valida persistência
```

---

## Faker / Fixture

### Caso 1

```
Faker
→ gera usuário único
→ Fixture cria usuário via API
→ teste utiliza usuário
```

---

## E2E

### Caso 1

```
API
→ preparação de usuário

Selenium
→ login
→ executar fluxo financeiro
→ validar resultado
```

---

# 32. O projeto deve demonstrar integração entre as tecnologias
O resultado final deve mostrar claramente:

```
                 ┌─────────────┐
                 │    JUnit    │
                 └──────┬──────┘
                        │
          ┌─────────────┼─────────────┐
          │             │             │
          ▼             ▼             ▼
      Selenium      Rest Assured     JDBC
          │             │             │
          ▼             ▼             ▼
         Web           API          PostgreSQL
          │             │             │
          └─────────────┼─────────────┘
                        │
                  Test Fixtures
                        │
                      Faker
                        │
                        ▼
                  GitHub Actions
```
Mas isso deve acontecer **sem criar uma camada de abstração para cada seta do diagrama**.

---

# 33. README
Reescreva completamente o README.

Ele deve explicar:

## 1. Objetivo
O que o projeto demonstra.

## 2. Tecnologias
Tabela simples:

TecnologiaObjetivoJavaLinguagemJUnit 5Test runnerSeleniumTestes WebRest AssuredTestes APIJDBCValidação no bancoPostgreSQLBancoFakerMassa dinâmicaJSON SchemaTestes de contratoDockerAmbienteGitHub ActionsCI
## 3. Arquitetura
Explique a estrutura de maneira simples.

## 4. Estratégia de testes
Explique:

- API;
- Web;
- Contract;
- Database;
- E2E.

## 5. Como executar localmente
Passo a passo.

## 6. Como executar API

## 7. Como executar Web

## 8. Como executar E2E

## 9. CI
Explique o workflow.

## 10. Test coverage
Crie uma pequena tabela mostrando os fluxos automatizados.

---

# 34. Não invente cobertura
Não coloque no README:

```
100% coverage
```
ou qualquer métrica que não possa ser comprovada.

O objetivo é mostrar **qualidade dos testes**, não criar números artificiais.

---

# 35. Comentários no código
Não encha o código de comentários explicando coisas óbvias.

Prefira código autoexplicativo.

Comentários devem existir somente quando explicarem:

- uma decisão não óbvia;
- uma limitação da aplicação;
- uma decisão de arquitetura;
- uma particularidade necessária para CI.

---

# 36. Qualidade do código
A implementação deve seguir:

- nomes claros;
- métodos pequenos;
- responsabilidades bem definidas;
- baixo acoplamento;
- baixo nível de abstração;
- ausência de duplicação relevante;
- código fácil de ler.
Não aplique SOLID ou Design Patterns artificialmente.

**Simplicidade é uma decisão arquitetural válida.**

---

# 37. Regra especialmente importante para este projeto
Este projeto será utilizado por uma pessoa que está aprendendo automação.

Portanto:

> **Não otimize o projeto para parecer sofisticado. Otimize o projeto para ser compreensível.**
Uma implementação simples e explícita é preferível a uma implementação abstrata que economiza algumas linhas de código.

Se existir uma escolha entre:

```
10 linhas simples
```
e:

```
3 linhas utilizando 5 abstrações
```
prefira as 10 linhas simples.

---

# 38. Processo de implementação
Não faça apenas uma grande geração de código sem verificar se funciona.

Execute o projeto incrementalmente.

Ordem recomendada:

### Etapa 1
Limpar o projeto atual.

### Etapa 2
Criar Maven + JUnit + configuração básica.

### Etapa 3
Implementar um teste Web simples.

### Etapa 4
Implementar Page Object.

### Etapa 5
Implementar primeiro teste de API.

### Etapa 6
Implementar API Client.

### Etapa 7
Implementar Fixture + Faker.

### Etapa 8
Implementar acesso PostgreSQL.

### Etapa 9
Implementar validação de persistência.

### Etapa 10
Implementar JSON Schema.

### Etapa 11
Implementar E2E.

### Etapa 12
Configurar GitHub Actions.

### Etapa 13
Executar todos os testes.

### Etapa 14
Corrigir problemas.

### Etapa 15
Atualizar README.

---

# 39. Validação final obrigatória
Antes de considerar o trabalho concluído, verifique:

```
[x] Projeto compila
[x] Testes API executam
[x] Testes Web executam
[x] Selenium funciona headless
[x] Page Objects estão sendo utilizados
[x] API Clients estão sendo utilizados
[x] Faker está sendo utilizado em massa dinâmica
[x] Fixture está sendo utilizada
[x] PostgreSQL é acessado pelos testes
[x] Existe pelo menos uma validação de persistência
[x] JSON Schema é validado
[ ] Existe pelo menos um fluxo E2E
[ ] Docker funciona
[ ] GitHub Actions funciona
[x] Falhas Web geram screenshot
[ ] Não existem Thread.sleep
[x] Não existem credenciais reais
[x] Não existem abstrações sem uso
[x] Não existem classes criadas apenas para "seguir pattern"
[x] README explica como executar tudo
```

Status verificado no estado atual do repositório:
- Atendidos: 14/17
- Pendentes: E2E real, Docker em execução local, validação de GitHub Actions
- Item que ainda conflita com a regra: `Thread.sleep` existe em `LoginPage` (`sleepQuietly`), então o ponto "Não existem Thread.sleep" permanece em aberto.

---

# 40. Resultado esperado
Ao final, o repositório deve transmitir a seguinte mensagem:

> "Este é um projeto de automação de testes desenvolvido em Java para uma aplicação financeira. Ele demonstra testes Web com Selenium e Page Object, testes de API com Rest Assured, testes de contrato, preparação de massa com Faker e Fixtures, validação de persistência utilizando PostgreSQL, fluxos E2E e execução automatizada em GitHub Actions."
O projeto **não precisa parecer um framework comercial**.

Ele precisa parecer um **projeto real de QA**, com testes bem pensados, código organizado e uma arquitetura que você consiga explicar e defender em uma entrevista.

---

# 41. Regra final
Se alguma parte do projeto atual conflitar com estas instruções, **priorize estas instruções**.

Você tem liberdade para apagar completamente a implementação atual e reconstruí-la.

Não preserve código apenas porque ele já existe.

Não preserve abstrações apenas porque parecem "boas práticas".

**Reconstrua o projeto de forma simples, funcional e didática, mantendo pelo menos um exemplo funcional de cada frente de teste descrita acima.**