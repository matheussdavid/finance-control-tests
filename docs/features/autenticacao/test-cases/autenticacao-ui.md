# Casos de Teste UI - Autenticação

**Data de geração:** 2026-09-22
**Autor:** QA Agent
**Fonte:** docs/features/autenticacao/autenticacao.md
**Última atualização:** 2026-09-23 (agrupado por página)

## Resumo Executivo
- **Total de casos de teste:** 15
- **Executados:** 4/15 (27%)
- **Distribuição por prioridade:**
  - P0: 3
  - P1: 7
  - P2: 5
- **Critérios de aceite cobertos:** 7 de 7 (UI)
- **Regras de negócio cobertas:** 6 de 6 (UI) — logout coberto por CT-012; regra 5 coberta por CT-010
- **Observações gerais:** CTs agrupados por página: `LoginPage` (modo login), `LoginPage` (modo register), Fluxo/navegação, `Navbar` (logout). Login e register compartilham a mesma tela com toggle. BVA na UI ficou restrito aos limites inferiores (2/8 caracteres): campos com `maxLength` (50/50/30) bloqueiam valores acima do máximo no browser, então não há como submeter >max via UI — coberto como observação em CT-014/CT-015. Estado via `TestUserFixture` (API). Mensagens exatas via i18n (`login.*`).

---

## Casos de Teste

### LoginPage (modo login)

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [x]    | CT-001 | Login com credenciais válidas exibe o dashboard | P0 | Positivo | CA-login-ui-feliz | #4 |
| [x]    | CT-013 | Login com email válido exibe o dashboard | P0 | Positivo | CA-login-ui-feliz | #4 |
| [x]    | CT-003 | Identificador em branco exibe validação e não chama a API | P1 | Negativo | CA-id-branco | #2 |
| [x]    | CT-004 | Senha menor que 8 caracteres exibe validação | P1 | Negativo | CA-senha-min | #2 |
| [x]    | CT-005 | Credenciais inválidas exibem erro da API e permanecem na página | P1 | Negativo | CA-login-erro-api | #3 |

### LoginPage (modo register)

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [x]    | CT-002 | Cadastro com dados válidos exibe o dashboard | P0 | Positivo | CA-register-ui-feliz | #4 |
| [x]    | CT-006 | Campos obrigatórios do cadastro em branco exibem validação | P1 | Negativo | CA-register-campos | #2 |
| [x]    | CT-007 | Email inválido no cadastro exibe validação | P1 | Negativo | CA-email-invalido-ui | #2 |
| [x]    | CT-008 | Senha e confirmação diferentes exibem validação | P1 | Negativo | CA-mismatch-ui | #2 |
| [x]    | CT-009 | Conflito de email/username exibe erro e permanece na página | P1 | Negativo | CA-conflito-ui | #3 |
| [x]    | CT-014 | Nome com 1 caractere no register exibe validação (BVA min 2) | P2 | Borda | CA-register-campos | #2 |
| [x]    | CT-015 | Username com 1 caractere no register exibe validação (BVA min 2) | P2 | Borda | CA-register-campos | #2 |

### Fluxo e navegação (LoginPage ↔ Dashboard)

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ]    | CT-010 | Usuário autenticado em /login é redirecionado para o dashboard | P2 | Borda | CA-redirect | #5 |
| [ ]    | CT-011 | Alternar entre login e cadastro navega e limpa o erro | P2 | Borda | CA-toggle | #1 |

### Navbar / Dashboard (logout)

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ]    | CT-012 | Logout na navbar limpa a sessão e redireciona para /login | P2 | Positivo | CA-logout | #6 |

---

## Detalhamento por página

## LoginPage (modo login)

### CT-001 - Login com credenciais válidas exibe o dashboard
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário registrado via API (`TestUserFixture`); tela `/login`
- **Passos:**
  1. Preencher `login-identifier-input` com o username
  2. Preencher `login-password-input` com a senha
  3. Clicar em `login-submit-btn`
- **Dados de entrada:** username/senha do `TestUser`
- **Resultado esperado:** URL `/`; `dashboard-page` visível
- **CA:** CA-login-ui-feliz - **Regra:** #4
- **Observacoes:** coberto por `LoginWebTest#deveAutenticarUsuarioComCredenciaisValidas`

### CT-013 - Login com email válido exibe o dashboard
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** usuário registrado via API (`TestUserFixture`); tela `/login`
- **Passos:**
  1. Preencher `login-identifier-input` com o email
  2. Preencher `login-password-input` com a senha
  3. Clicar em `login-submit-btn`
- **Dados de entrada:** email/senha do `TestUser`
- **Resultado esperado:** URL `/`; `dashboard-page` visível (identifier aceita email — regra #1)
- **CA:** CA-login-ui-feliz - **Regra:** #4
- **Observacoes:** adicionado a partir de `LoginWebTest#deveAutenticarUsuarioComEmailValido` (faltava no doc)

### CT-003 - Identificador em branco exibe validação e não chama a API
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/login`
- **Passos:**
  1. Deixar `login-identifier-input` vazio; preencher senha válida
  2. Clicar em `login-submit-btn`
- **Dados de entrada:** identifier vazio
- **Resultado esperado:** `Message` com `login.identifierRequired`; permanece em `/login`; nenhuma chamada a `/auth/login`
- **CA:** CA-id-branco - **Regra:** #2
- **Observacoes:** garantir "sem chamada" por construção (submeter formulário inválido)

### CT-004 - Senha menor que 8 caracteres exibe validação
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/login`
- **Passos:**
  1. Preencher identifier válido; senha com 7 caracteres
  2. Clicar em `login-submit-btn`
- **Dados de entrada:** senha "1234567" (mínimo 8 — BVA: min-1)
- **Resultado esperado:** `Message` com `login.passwordMin` ("A senha deve ter no mínimo 8 caracteres"); permanece em `/login`
- **CA:** CA-senha-min - **Regra:** #2
- **Observacoes:** o mínimo **client e server é 8** (conferido no frontend e no `RegisterRequest`). Aplicar também ao modo register (mesma chave). Coberto por `LoginWebTest#deveExibirMensagemQuandoSenhaMenorQue8Caracteres`

### CT-005 - Credenciais inválidas exibem erro da API e permanecem na página
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** usuário registrado via API
- **Passos:**
  1. Preencher `login-identifier-input` com username válido; senha errada
  2. Clicar em `login-submit-btn`
- **Dados de entrada:** senha incorreta
- **Resultado esperado:** `Message` com a mensagem de erro da API (401, pt-BR); permanece em `/login`; `dashboard-page` ausente
- **CA:** CA-login-erro-api - **Regra:** #3
- **Observacoes:** coberto por `LoginWebTest#deveExibirMensagemQuandoUsuarioInformaCredenciaisInvalidas`

## LoginPage (modo register)

### CT-002 - Cadastro com dados válidos exibe o dashboard
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** tela `/register` (ou toggle)
- **Passos:**
  1. Preencher `register-name-input`, `register-username-input`, `register-email-input`, `login-password-input`, `register-confirm-password-input`
  2. Clicar em `login-submit-btn`
- **Dados de entrada:** massa faker única (name, username, email, senha, confirmação)
- **Resultado esperado:** URL `/`; `dashboard-page` visível (login implícito)
- **CA:** CA-register-ui-feliz - **Regra:** #4
- **Observacoes:** massa via `UserFaker` (nunca fixa). Pendente de automação (criar `RegisterWebTest`).

### CT-006 - Campos obrigatórios do cadastro em branco exibem validação
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/register`
- **Passos:**
  1. Submeter com um campo obrigatório em branco por vez (name → username → email → password → confirm)
  2. Verificar a mensagem de validação correspondente
- **Dados de entrada:** payloads parametrizados (campo vazio)
- **Resultado esperado:** mensagem da ordem de checagem (nameRequired → usernameRequired → emailRequired → passwordRequired → confirmPasswordRequired); nenhuma chamada à API
- **CA:** CA-register-campos - **Regra:** #2
- **Observacoes:** [SUPOSICAO] ordem fixa; pode ser um caso parametrizado

### CT-007 - Email inválido no cadastro exibe validação
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/register`
- **Passos:**
  1. Preencher `register-email-input` com "x@" (sem domínio); demais campos corretos
  2. Clicar em `login-submit-btn`
- **Dados de entrada:** email sem domínio
- **Resultado esperado:** `Message` com `login.emailInvalid`; permanece na página
- **CA:** CA-email-invalido-ui - **Regra:** #2
- **Observacoes:** regex client: `/^\S+@\S+\.\S+$/`

### CT-008 - Senha e confirmação diferentes exibem validação
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** tela `/register`
- **Passos:**
  1. Preencher senha e confirmação distintas (ambas >= 8)
  2. Clicar em `login-submit-btn`
- **Dados de entrada:** `password` != `confirmPassword`
- **Resultado esperado:** `Message` com `login.passwordMismatch`; nenhuma chamada à API
- **CA:** CA-mismatch-ui - **Regra:** #2
- **Observacoes:** -

### CT-009 - Conflito de email/username exibe erro e permanece na página
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** usuário de apoio via API; tela `/register`
- **Passos:**
  1. Preencher cadastro reusando o username (ou email) do usuário de apoio
  2. Clicar em `login-submit-btn`
- **Dados de entrada:** campo conflitante + demais únicos
- **Resultado esperado:** `Message` com o 409 da API (pt-BR); permanece na página; não autentica
- **CA:** CA-conflito-ui - **Regra:** #3
- **Observacoes:** reutilizar `TestUserFixture`; cobrir as duas variações (username/email) parametrizado

### CT-014 - Nome com 1 caractere no register exibe validação (BVA min 2)
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** tela `/register`
- **Passos:**
  1. Preencher `register-name-input` com 1 caractere; demais campos corretos
  2. Clicar em `login-submit-btn`
- **Dados de entrada:** name com 1 caractere (mínimo 2 — BVA: min-1)
- **Resultado esperado:** `Message` com `login.nameTooShort`; permanece na página; nenhuma chamada à API
- **CA:** CA-register-campos - **Regra:** #2
- **Observacoes:** BVA de máximo não se aplica na UI: `maxLength={50}` bloqueia digitação acima de 50. Validar também 2 caracteres como happy path (dentro do CT-002).

### CT-015 - Username com 1 caractere no register exibe validação (BVA min 2)
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** tela `/register`
- **Passos:**
  1. Preencher `register-username-input` com 1 caractere; demais campos corretos
  2. Clicar em `login-submit-btn`
- **Dados de entrada:** username com 1 caractere (mínimo 2 — BVA: min-1)
- **Resultado esperado:** `Message` com `login.usernameTooShort`; permanece na página; nenhuma chamada à API
- **CA:** CA-register-campos - **Regra:** #2
- **Observacoes:** BVA de máximo não se aplica na UI: `maxLength={50}` bloqueia digitação acima de 50.

## Fluxo e navegação (LoginPage ↔ Dashboard)

### CT-010 - Usuário autenticado em /login é redirecionado para o dashboard
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** sessão autenticada (token em `localStorage`)
- **Passos:**
  1. Navegar para `/login` com sessão válida
  2. Verificar redirect
- **Dados de entrada:** token de sessão ativo
- **Resultado esperado:** redirecionado para `/` (`<Navigate to="/" replace/>`)
- **CA:** CA-redirect - **Regra:** #5
- **Observacoes:** login via API para popular `localStorage` antes

### CT-011 - Alternar entre login e cadastro navega e limpa o erro
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** tela `/login`
- **Passos:**
  1. Clicar em `login-toggle-mode-btn`
  2. Verificar URL `/register` e ausência de error
  3. Alternar de volta
- **Dados de entrada:** -
- **Resultado esperado:** navegação `/login` ↔ `/register`; erro anterior limpo; campos corretos por modo
- **CA:** CA-toggle - **Regra:** #1
- **Observacoes:** -

## Navbar / Dashboard (logout)

### CT-012 - Logout na navbar limpa a sessão e redireciona para /login
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** sessão autenticada (via login na UI ou API + `localStorage`)
- **Passos:**
  1. Abrir o menu do usuário (`layout-topnav-user-menu-btn`)
  2. Clicar em `layout-topnav-user-logout-btn`
- **Dados de entrada:** -
- **Resultado esperado:** token removido do `localStorage`; redirecionado para `/login`; ao tentar acessar rota autenticada, volta para `/login`
- **CA:** CA-logout - **Regra:** #6
- **Observacoes:** logout é client-side (sem chamada de backend)

---