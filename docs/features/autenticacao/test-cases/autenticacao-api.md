# Casos de Teste API - Autenticação

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/autenticacao/autenticacao.md
**Última atualização:** 2026-09-23 (agrupado por endpoint)

## Resumo Executivo
- **Total de casos de teste:** 24
- **Executados:** 24/24 (100%)
- **Distribuição por prioridade:**
  - P0: 7
  - P1: 15
  - P2: 2
- **Critérios de aceite cobertos:** 10 de 10 (API) — positivos e negativos; autorização (token) coberto por CT-015/CT-016
- **Regras de negócio cobertas:** 9/9 — regra 9 (sessão stateless) coberta explicitamente por CT-015/CT-016
- **Observações gerais:** CTs agrupados por endpoint (`POST /auth/login`, `POST /auth/register`, autorização/regra #9). Os 3 contratos de sucesso/erro (login/register) já têm schema e transagem. CT-017/CT-018/CT-019 aplicam BVA nos limites reais do `RegisterRequest` (name/username 2-50, email ≤ 50, senha/confirmação 8-30 — conferidos no backend). Login não tem BVA de comprimento: `LoginRequest` só valida `@NotBlank` (identifier/password); limites vazios/nulos/espaços são cobertos por CT-021/022/023/024. CT-015/CT-016 usam `GET /accounts` somente como alvo do filtro de autorização (dedupe com feature contas).

---

## Casos de Teste

### POST /auth/login

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [x]    | CT-001 | Contrato do response de sucesso do POST /auth/login | P0 | Contrato | - | - |
| [x]    | CT-002 | Contrato do response de erro do POST /auth/login | P0 | Contrato | - | - |
| [x]    | CT-005 | Login com username válido retorna token e usuário | P0 | Positivo | CA-login-usuario | #1 |
| [x]    | CT-006 | Login com email válido retorna token | P0 | Positivo | CA-login-email | #1 |
| [x]    | CT-008 | Login com credenciais inválidas retorna 401 UNAUTHORIZED | P1 | Negativo | CA-login-invalido | #2 |
| [x]    | CT-021 | Login com identifier/password vazios retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-login-vazio | #7 |
| [x]    | CT-022 | Login com body JSON vazio retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-login-vazio | #7 |
| [x]    | CT-023 | Login com campos explicitamente nulos retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-login-vazio | #7 |
| [x]    | CT-024 | Login com campos contendo apenas espaços retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-login-vazio | #7 |

### POST /auth/register

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [x]    | CT-003 | Contrato do response de sucesso do POST /auth/register | P0 | Contrato | - | - |
| [x]    | CT-004 | Contrato do response de erro do POST /auth/register | P0 | Contrato | - | - |
| [x]    | CT-007 | Cadastro com dados válidos cria usuário e retorna token | P0 | Positivo | CA-register-feliz | #8 |
| [x]    | CT-009 | Cadastro com email já cadastrado retorna 409 | P1 | Negativo | CA-email-dup | #3 |
| [x]    | CT-010 | Cadastro com username já cadastrado retorna 409 | P1 | Negativo | CA-username-dup | #4 |
| [x]    | CT-011 | Cadastro com senha e confirmação diferentes retorna 409 | P1 | Negativo | CA-password-mismatch | #5 |
| [x]    | CT-012 | Campos obrigatórios ausentes retornam 400 VALIDATION_ERROR com fields | P1 | Negativo | CA-campos-ausentes | #7 |
| [x]    | CT-013 | Cadastro com email malformado retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-email-invalido | #7 |
| [x]    | CT-014 | Cadastro com senha abaixo do mínimo retorna 400 VALIDATION_ERROR | P1 | Negativo | CA-senha-curta | #6 |
| [x]    | CT-017 | Limites da senha: 7/8/30/31 caracteres (BVA) | P1 | Borda | CA-senha-limites | #6 |
| [x]    | CT-018 | Limites de name/username: 1/2/50/51 caracteres (BVA) | P1 | Borda | CA-name-username-limites | #7 |
| [x]    | CT-019 | Limite do email: 49/50/51 caracteres (BVA) | P1 | Borda | CA-email-limite | #7 |
| [x]    | CT-020 | Cadastro com senha só de espaços retorna 400 VALIDATION_ERROR | P2 | Borda | CA-senha-espacos | #6 |

### Autorização / sessão stateless (regra #9) — alvo: GET /accounts

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [x]    | CT-015 | Endpoint autenticado sem token retorna 401 UNAUTHORIZED | P1 | Negativo | CA-token-ausente | #9 |
| [x]    | CT-016 | Endpoint autenticado com token inválido retorna 401 UNAUTHORIZED | P2 | Negativo | CA-token-ausente | #9 |

---

## Detalhamento por endpoint

## POST /auth/login

### CT-001 - Contrato do response de sucesso do POST /auth/login
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/auth/login-response.json` (existe)
- **Pre-condicoes:** usuário registrado via API (`TestUserFixture`)
- **Passos:**
  1. Enviar `POST /auth/login` com `identifier` = username e senha correta
  2. Validar o corpo contra o schema estrito
- **Dados de entrada:** `{"identifier": "<username>", "password": "<senha>"}`
- **Resultado esperado:** HTTP 200; body respeita o JSON Schema (
  `additionalProperties: false`); qualquer campo ausente/extra/tipo errado falha
- **CA:** - **Regra:** - **Observacoes:** coberto por `LoginApiTest#deveRetornar200QuandoLoginForValidoEValidarContrato`

### CT-002 - Contrato do response de erro do POST /auth/login
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/auth/error-response.json` (existe)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/login` com senha errada
  2. Validar o corpo de erro (401) contra o schema
- **Dados de entrada:** `{"identifier": "x", "password": "errada"}`
- **Resultado esperado:** HTTP 401; body `{timestamp,status,error,message,path}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** coberto por `LoginApiTest#deveRetornar401QuandoLoginForInvalidoEValidarContrato`

### CT-005 - Login com username válido retorna token e usuário
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário registrado via API (`TestUserFixture`)
- **Passos:**
  1. Enviar `POST /auth/login` com `identifier` = username e senha correta
  2. Extrair `token` e `user`
  3. Asserir token não-branco e `user.id` = id do usuário cadastrado
- **Dados de entrada:** `{"identifier": "<username>", "password": "<senha>"}`
- **Resultado esperado:** HTTP 200; token não-branco; `user.id`/`user.email` corretos
- **CA:** CA-login-usuario - **Regra:** #1
- **Observacoes:** coberto por `LoginApiTest#deveRetornar200QuandoUsernameESenhaForemValidos`

### CT-006 - Login com email válido retorna token
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** usuário registrado via API
- **Passos:**
  1. Enviar `POST /auth/login` com `identifier` = email e senha correta
  2. Extrair `token`
- **Dados de entrada:** `{"identifier": "<email>", "password": "<senha>"}`
- **Resultado esperado:** HTTP 200; token não-branco (`identifier` aceita email)
- **CA:** CA-login-email - **Regra:** #1
- **Observacoes:** coberto por `LoginApiTest#deveRetornar200QuandoEmailESenhaForemValidos`

### CT-008 - Login com credenciais inválidas retorna 401 UNAUTHORIZED
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** usuário registrado via API
- **Passos:**
  1. Enviar `POST /auth/login` com senha incorreta
  2. Asserir status e shape de erro
- **Dados de entrada:** `{"identifier": "<username>", "password": "errada"}`
- **Resultado esperado:** HTTP 401; `error: UNAUTHORIZED` (sem token)
- **CA:** CA-login-invalido - **Regra:** #2
- **Observacoes:** coberto por `LoginApiTest#deveRetornar401QuandoCredenciaisForemInvalidas`

### CT-021 - Login com identifier/password vazios retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/login` com `identifier` vazio (password válida)
  2. Repetir com `password` vazio (identifier válido)
  3. Repetir com ambos vazios
  4. Asserir 400 e `fields.<campo>` apontando o campo
- **Dados de entrada:** payloads parametrizados: `"identifier": ""`, `"password": ""`, ambos `""`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.identifier` ("Usuário ou e-mail é obrigatório") e/ou `fields.password` ("A senha é obrigatória")
- **CA:** CA-login-vazio - **Regra:** #7
- **Observacoes:** coberto por 3 métodos de `LoginApiTest` (identifier/password/ambos vazios)

### CT-022 - Login com body JSON vazio retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/login` com body `{}`
  2. Asserir 400
- **Dados de entrada:** `{}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.identifier` e `fields.password`
- **CA:** CA-login-vazio - **Regra:** #7
- **Observacoes:** coberto por `LoginApiTest#deveRetornar400QuandoBodyEstiverVazio`

### CT-023 - Login com campos explicitamente nulos retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/login` com `identifier: null` e `password: null`
  2. Asserir 400
- **Dados de entrada:** `{"identifier": null, "password": null}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.identifier` e `fields.password`
- **CA:** CA-login-vazio - **Regra:** #7
- **Observacoes:** coberto por `LoginApiTest#deveRetornar400QuandoCamposForemNulos`

### CT-024 - Login com campos contendo apenas espaços retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/login` com `identifier: "   "` e `password: "   "`
  2. Asserir 400
- **Dados de entrada:** `{"identifier": "   ", "password": "   "}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.identifier` e `fields.password` (`@NotBlank`)
- **CA:** CA-login-vazio - **Regra:** #7
- **Observacoes:** coberto por `LoginApiTest#deveRetornar400QuandoCamposContiveremApenasEspacos`

## POST /auth/register

### CT-003 - Contrato do response de sucesso do POST /auth/register
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/auth/register-response.json` (existe)
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/register` com payload válido e único
  2. Validar o corpo (201) contra o schema estrito
- **Dados de entrada:** user faker + `confirmPassword` = `password`
- **Resultado esperado:** HTTP 201; body `{token,user{id,name,email}}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** coberto por `RegisterApiTest#deveRetornar200QuandoRegisterForValidoEValidarContrato`

### CT-004 - Contrato do response de erro do POST /auth/register
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/auth/error-response.json` (existe)
- **Pre-condicoes:** email já cadastrado
- **Passos:**
  1. Enviar `POST /auth/register` reusando um email existente
  2. Validar o corpo de erro (409) contra o schema
- **Dados de entrada:** payload com `email` duplicado
- **Resultado esperado:** HTTP 409; body `{timestamp,status,error,message,path}` conforme schema
- **CA:** - **Regra:** - **Observacoes:** formato do erro de negócio = `{timestamp,status,error,message,path}` **sem `fields`** — só 400 `VALIDATION_ERROR` traz `fields` (campo ausente oculto por `non_null`). Automatizado em `RegisterApiTest#deveValidarContratoDeErroDoRegister` (assert `fields == null` + JSON Schema).

### CT-007 - Cadastro com dados válidos cria usuário e retorna token
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/register` com payload válido e único
  2. Asserir 201, token não-branco e `user` conforme dados enviados
  3. Usar o token em `GET /accounts` para provar autenticação
- **Dados de entrada:** `TestUser` faker (name, username, email, password, confirmPassword)
- **Resultado esperado:** HTTP 201; token não-branco; `GET /accounts` autenticado retorna 200
- **CA:** CA-register-feliz - **Regra:** #8
- **Observacoes:** coberto por `RegisterApiTest#deveRetornar200QuandoCadastraUmUsuarioComDadosValidos` (passo 3 opcional)

### CT-009 - Cadastro com email já cadastrado retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** um usuário já cadastrado com o mesmo email
- **Passos:**
  1. Enviar `POST /auth/register` reusando o email existente (username novo)
  2. Asserir 409
- **Dados de entrada:** email do usuário de apoio + username/name novos
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` (`auth.emailAlreadyRegistered`)
- **CA:** CA-email-dup - **Regra:** #3
- **Observacoes:** coberto por `RegisterApiTest#deveRetornar409AoTentarCadastrarUmUsuarioComEmailJaCadastrado`

### CT-010 - Cadastro com username já cadastrado retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** um usuário já cadastrado com o mesmo username
- **Passos:**
  1. Enviar `POST /auth/register` reusando o username existente (email novo)
  2. Asserir 409
- **Dados de entrada:** username do usuário de apoio + email/name novos
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` (`auth.usernameAlreadyRegistered`)
- **CA:** CA-username-dup - **Regra:** #4
- **Observacoes:** coberto por `RegisterApiTest#deveRetornar409AoTentarCadastrarUmUsuarioComUsernameEmailJaCadastrado`

### CT-011 - Cadastro com senha e confirmação diferentes retorna 409
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/register` com `password != confirmPassword`
  2. Asserir 409
- **Dados de entrada:** senhas distintas válidas isoladamente (8-30)
- **Resultado esperado:** HTTP 409 `BUSINESS_RULE_VIOLATION` (`auth.passwordMismatch`) — surpreendente: não é 400
- **CA:** CA-password-mismatch - **Regra:** #5
- **Observacoes:** coberto por `RegisterApiTest#deveRetornar409AoTentarCadastrarUmUsuarioComSenhaDiferenteDaConfirmacao`

### CT-012 - Campos obrigatórios ausentes retornam 400 VALIDATION_ERROR com fields
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/register` com um campo obrigatório ausente por vez (name, username, email, password, confirmPassword)
  2. Asserir 400 e `fields` apontando o campo
- **Dados de entrada:** payloads parametrizados com campo ausente (versão base: `{}`)
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` com `fields.<campo>` preenchido
- **CA:** CA-campos-ausentes - **Regra:** #7
- **Observacoes:** coberto por `RegisterApiTest#deveRetornar400AoTentarCadastrarUsuarioSemInformarOsCamposObrigatorios` (body `{}` valida os 5 campos de uma vez)

### CT-013 - Cadastro com email malformado retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/register` com `email` sem `@`/domínio
  2. Asserir 400 e `fields.email`
- **Dados de entrada:** `"email": "email.com.br"}`
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`fields.email` = "O e-mail deve ser válido")
- **CA:** CA-email-invalido - **Regra:** #7
- **Observacoes:** coberto por `RegisterApiTest#deveRetornar400AoTentarCadastrarUmUsuarioComEmailInvalido`

### CT-014 - Cadastro com senha abaixo do mínimo retorna 400 VALIDATION_ERROR
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/register` com `password` e `confirmPassword` abaixo de 8 caracteres
  2. Asserir 400 e `fields.password`
- **Dados de entrada:** senha de 5 caracteres (mínimo 8); caso isolado fora do BVA parametrizado
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`fields.password` = "A senha deve ter entre 8 e 30 caracteres")
- **CA:** CA-senha-curta - **Regra:** #6
- **Observacoes:** coberto por `RegisterApiTest#deveRetornar400AoTentarCadastrarUmUsuarioComSenhaMenorAbaixoDoMinimo`; a borda completa (7/8/30/31) fica no CT-017

### CT-017 - Limites da senha: 7/8/30/31 caracteres (BVA)
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/register` com `password` = `confirmPassword` = 7 caracteres → 400
  2. Repetir com 8 caracteres → 201
  3. Repetir com 30 caracteres → 201
  4. Repetir com 31 caracteres → 400
- **Dados de entrada:** senhas de 7/8/30/31 caracteres (BVA: min-1/min/max/max+1)
- **Resultado esperado:** 7 e 31 → 400 `VALIDATION_ERROR` (`fields.password`/`fields.confirmPassword`); 8 e 30 → 201
- **CA:** CA-senha-limites - **Regra:** #6
- **Observacoes:** limites reais do `RegisterRequest` conferidos no backend (`@Size(min=8, max=30)`). Parametrizável. Automatizado em `RegisterApiTest#deveValidarLimitesDaSenha`.

### CT-018 - Limites de name/username: 1/2/50/51 caracteres (BVA)
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/register` com `name` de 1 caractere e `username` válido → 400
  2. Repetir com `name` = username = 2 caracteres → 201
  3. Repetir com `name` = username = 50 caracteres → 201
  4. Repetir com `name`/`username` de 51 caracteres → 400
- **Dados de entrada:** name/username de 1/2/50/51 caracteres (BVA: min-1/min/max/max+1)
- **Resultado esperado:** 1 e 51 → 400 `VALIDATION_ERROR` (`fields.name`/`fields.username`); 2 e 50 → 201
- **CA:** CA-name-username-limites - **Regra:** #7
- **Observacoes:** limites reais do `RegisterRequest` conferidos no backend (`@Size(min=2, max=50)`). Parametrizável. Automatizado em casos separados: `RegisterApiTest#deveValidarLimitesDoCampoNome` (campos.name) e `RegisterApiTest#deveValidarLimitesDoCampoUsername` (campos.username).

### CT-019 - Limite do email: 49/50/51 caracteres (BVA)
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/register` com `email` de 50 caracteres válido (local ≤ 64, labels domínio ≤ 63) → 201
  2. Enviar `POST /auth/register` com `email` de 51 caracteres → 400
- **Dados de entrada:** emails com parte total de 49/50/51 caracteres (BVA: max-1/max/max+1; 49 só confirmatório)
- **Resultado esperado:** 49 e 50 → 201; 51 → 400 `VALIDATION_ERROR` (`fields.email` = "O e-mail deve ter no máximo 50 caracteres")
- **CA:** CA-email-limite - **Regra:** #7
- **Observacoes:** limites reais do `RegisterRequest` conferidos no backend (`@Size(max=50)`). Restrições estruturais do `@Email`: local part ≤ 64, label do domínio ≤ 63 (IDN). Automatizado em `RegisterApiTest#deveValidarLimitesDoCampoEmail`.

### CT-020 - Cadastro com senha só de espaços retorna 400 VALIDATION_ERROR
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `POST /auth/register` com `password` = `confirmPassword` = `" "`
  2. Asserir 400
- **Dados de entrada:** senha e confirmação com espaço em branco
- **Resultado esperado:** HTTP 400 `VALIDATION_ERROR` (`fields.password` e `fields.confirmPassword` = obrigatórias por `@NotBlank`)
- **CA:** CA-senha-espacos - **Regra:** #6
- **Observacoes:** coberto por `RegisterApiTest#deveRetornar400AoTentarCadastrarUmUsuarioComSenhaEConfirmacaoSendoEspacos`

## Autorização / sessão stateless (regra #9) — alvo: GET /accounts

### CT-015 - Endpoint autenticado sem token retorna 401 UNAUTHORIZED
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /accounts` sem `Authorization`
  2. Asserir 401
- **Dados de entrada:** sem header
- **Resultado esperado:** HTTP 401 `UNAUTHORIZED`
- **CA:** CA-token-ausente - **Regra:** #9
- **Observacoes:** testa o **filtro global de autorização** (regra #9), não a feature contas — `GET /accounts` é apenas alvo. Rotas públicas são só `/auth/*`, `/error` e Swagger. Dedupe: feature contas não duplica este CT (`contas-revisao.md`). Automatizado em `AuthorizationApiTest#deveRetornar401QuandoRequisicaoAutenticadaSemToken`.

### CT-016 - Endpoint autenticado com token inválido retorna 401 UNAUTHORIZED
- **Prioridade:** P2
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** nenhuma
- **Passos:**
  1. Enviar `GET /accounts` com `Authorization: Bearer <token-forjado>`
  2. Asserir 401
- **Dados de entrada:** token aleatório/assinatura inválida
- **Resultado esperado:** HTTP 401 `UNAUTHORIZED`
- **CA:** CA-token-ausente - **Regra:** #9
- **Observacoes:** [SUPOSICAO] confirmada na automação: filtro trata token inválido como ausente (resposta 401 idêntica). Automatizado em `AuthorizationApiTest#deveRetornar401QuandoRequisicaoAutenticadaComTokenInvalido`.

---