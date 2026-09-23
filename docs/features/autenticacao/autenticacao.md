# História: Autenticação

## Descrição

Permite que um usuário crie sua conta (register) e entre na aplicação (login).
O login aceita **email OU username** no campo `identifier`. Cadastro e login
retornam um token JWT (Bearer, HS256, 24h) e o resumo do usuário
(`id`, `name`, `email` — **username não é retornado**). Não existe endpoint de
logout: o cliente apenas limpa o `localStorage`.

O fluxo cobre unicidade de email/username, validação JSR-303 dos campos,
conferência de senhas no cadastro e rejeição de credenciais inválidas.

---

## API

### Endpooints

- **`POST /auth/register`** → **201 Created** (autenticação requerida: **não**)
- **`POST /auth/login`** → **200 OK** (autenticação requerida: **não**)
- `POST /auth/logout` — **não existe** (logout é client-side)

### Request — `POST /auth/register`

```json
{
  "name": "Novo Usuário",              // Obrigatório, 2–50
  "username": "novo_user",             // Obrigatório, 2–50, único
  "email": "novo@example.com",         // Obrigatório, formato e-mail, ≤ 50, único
  "password": "senha123",              // Obrigatório, 8–30
  "confirmPassword": "senha123"        // Obrigatório, 8–30, deve ser igual a password
}
```

### Request — `POST /auth/login`

```json
{
  "identifier": "novo_user",           // Obrigatório; aceita email OU username
  "password": "senha123"               // Obrigatório
}
```

### Response — ambos os endpoints

```json
{
  "token": "<jwt>",
  "user": { "id": "<uuid>", "name": "Novo Usuário", "email": "novo@example.com" }
}
```

### Shape de erro (compartilhado)

```json
{
  "timestamp": "...",
  "status": 409,
  "error": "BUSINESS_RULE_VIOLATION",
  "message": "...",
  "path": "...",
  "fields": null
}
```

`fields` (map campo→mensagem) só é preenchido em 400 de validação. Erros já chegam
traduzidos para pt-BR.

### Status usados nesta feature

| Cenário | HTTP | `error` |
|---|---|---|
| Credenciais inválidas (login) | 401 | `UNAUTHORIZED` |
| Email já cadastrado (register) | 409 | `BUSINESS_RULE_VIOLATION` |
| Username já cadastrado (register) | 409 | `BUSINESS_RULE_VIOLATION` |
| Senha ≠ confirmação (register) | 409 | `BUSINESS_RULE_VIOLATION` |
| Campo obrigatório ausente / tamanho/email inválidos | 400 | `VALIDATION_ERROR` (+ `fields`) |

---

## Regras de negócio (API)

1. **Login por email OU username:** `identifier` é resolvido por
   `UserRepository.findByEmailOrUsername` — um único campo aceita ambos.
2. **Credenciais inválidas → 401 `UNAUTHORIZED`** (`auth.invalidCredentials`).
3. **Email único:** cadastro com email já usado → 409 (`auth.emailAlreadyRegistered`).
4. **Username único:** cadastro com username já usado → 409 (`auth.usernameAlreadyRegistered`).
5. **Conferência de senha:** `password != confirmPassword` → 409
   (`auth.passwordMismatch`) — **surpreendente: é 409, não 400**.
6. **Senha forte:** BCrypt; validação `@NotBlank` + `@Size(min=8, max=30)`.
7. **Validação de forma (400 `VALIDATION_ERROR`):** name/username 2–50,
   email válido (`@Email`) ≤ 50, senha/confirmação 8–30; campos de
   `confirmPassword` validados só com `@NotBlank`/`@Size` (a igualdade é regra 5).
8. **Login implícito no register:** cadastro bem-sucedido já retorna token+user
   (front é redirecionado direto para `/`).
9. **Sessão stateless:** token JWT `Authorization: Bearer <token>`, expiração 24h.
   Sem refresh, sem logout no servidor.

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** um usuário com username/email únicos e senha = confirmação (8–30)
- **Quando** envio `POST /auth/register`
- **Então** recebo 201 com `token` não-branco e `user{id,name,email}` corretos

- **Dado** um usuário recém-cadastrado
- **Quando** envio `POST /auth/login` com `identifier` = **username** e senha correta
- **Então** recebo 200 com token não-branco e `user.id` = id do cadastro

- **Dado** o mesmo usuário
- **Quando** envio `POST /auth/login` com `identifier` = **email** e senha correta
- **Então** recebo 200 com token não-branco

### Casos negativos / borda
- **Dado** senha incorreta no login
- **Quando** envio `POST /auth/login`
- **Então** recebo 401 `UNAUTHORIZED`

- **Dado** email já cadastrado
- **Quando** tento cadastrar com o mesmo email
- **Então** recebo 409 (`auth.emailAlreadyRegistered`)

- **Dado** username já cadastrado
- **Quando** tento cadastrar com o mesmo username
- **Então** recebo 409 (`auth.usernameAlreadyRegistered`)

- **Dado** `password != confirmPassword`
- **Quando** envio o cadastro
- **Então** recebo 409 (`auth.passwordMismatch`)

- **Dado** campo obrigatório ausente, email malformado, name/username fora do
  intervalo 2–50 ou senha < 8
- **Quando** envio o cadastro ou o login
- **Então** recebo 400 `VALIDATION_ERROR` com `fields` apontando o campo

- **Dado** token ausente ou inválido
- **Quando** chamo um endpoint autenticado (ex.: `GET /accounts`)
- **Então** recebo 401 `UNAUTHORIZED`

---

## UI — Fluxo / comportamento

Fonte: `frontend/src/pages/LoginPage.tsx`, `frontend/src/hooks/useAuth.tsx`,
`frontend/src/routes/AppRoutes.tsx`.

### Rotas e telas
- `/login` e `/register` **compartilham a mesma tela** (`LoginPage`); o toggle
  (`login-toggle-mode-btn`) alterna o modo e navega entre as rotas.
- Usuário já autenticado acessando `/login`/`/register` → redirecionado para `/`.
- 401 em rota autenticada → limpa token e redireciona para `/login`.

### Campos do formulário
| Modo | Campo | testid | Validação client |
|---|---|---|---|
| login | User or Email | `login-identifier-input` | `login.identifierRequired` |
| login | Password | `login-password-input` | `login.passwordRequired`, `login.passwordMin` |
| register | Name | `login-name-input` | `login.nameRequired`, `login.nameTooShort` (min 2) |
| register | Username | `login-username-input` | `login.usernameRequired`, `login.usernameTooShort` (min 2) |
| register | Email | `login-email-input` | `login.emailRequired`, `login.emailInvalid` |
| register | Password | `login-password-input` | `login.passwordRequired`, `login.passwordMin` (min 8) |
| register | Confirm Password | `login-confirm-password-input` | `login.confirmPasswordRequired`, `login.passwordMismatch` |
| ambos | Submit | `login-submit-btn` | — |
| ambos | Toggle | `login-toggle-mode-btn` | — |

`maxLength`: name 50, username 50, email 50, password/confirm 30 (browser bloqueia > máximo — sem BVA de max na UI).

### Comportamento
- **Validação client (ordem de checagem)** — login: identifier em branco →
  password em branco → password < 8. Register: name em branco → name < 2 →
  username em branco → username < 2 → email em branco → formato email → password
  em branco → password < 8 → confirm em branco → senhas distintas.
- Erro de validação client é exibido em `Message` (`login-form`) e **não** chama a API.
- Erro da API é formatado (`formatApiError`) e exibido no mesmo `Message`; sem navigate.
- Sucesso → `navigate('/')` (dashboard).
- Erro de API com `fields` → `mensagem (campo: mensagem; ...)`.

### data-testids relevantes
`login-page`, `login-form-wrapper`, `login-form-title`, `login-form`,
`login-name-input`, `login-username-input`, `login-identifier-input`,
`login-email-input`, `login-password-input`, `login-confirm-password-input`,
`login-submit-btn`, `login-toggle-mode-btn`.

---

## Regras de negócio (UI)

1. **Login e register numa única tela**, alternando por toggle/rota; formulário
   dinâmico conforme o modo.
2. **Validação client** bloqueia o submit antes de tocar a API (ordem fixa).
3. **Erro de API** exibido em `Message`; permanece na página (não navega).
4. **Sucesso** autentica e redireciona para `/` (login implícito).
5. **Redirecionamento**: já autenticado em `/login` → `/`; 401 em rota
   autenticada → `/login`.
6. **Logout** (navbar `layout-topnav-user-logout-btn`) só limpa `localStorage`
   (sem chamada de backend).

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** um usuário já cadastrado (via API)
- **Quando** preencho identifier + senha corretos e clico em entrar
- **Então** sou autenticado e redirecionado para o dashboard (`dashboard-page`)

- **Dado** um formulário de register preenchido corretamente (senha = confirmação)
- **Quando** clico em criar conta
- **Então** sou autenticado e redirecionado para o dashboard

### Casos negativos / borda
- **Dado** identifier em branco
- **Quando** tento entrar
- **Então** exibe `login.identifierRequired` e **não** chama a API

- **Dado** senha < 8 (login ou register)
- **Quando** tento submeter
- **Então** exibe `login.passwordMin` e **não** chama a API

- **Dado** credenciais inválidas
- **Quando** submeto o login
- **Então** exibe a mensagem de erro da API e **permaneço** na página

- **Dado** register com email inválido
- **Quando** submeto
- **Então** exibe `login.emailInvalid` e **não** chama a API

- **Dado** register com `password != confirmPassword`
- **Quando** submeto
- **Então** exibe `login.passwordMismatch` e **não** chama a API

- **Dado** register com email ou username já em uso
- **Quando** submeto
- **Então** exibe o erro 409 da API e **permaneço** na página

- **Dado** um usuário já autenticado
- **Quando** acessa `/login`
- **Então** é redirecionado para `/`

---

## Referência (código-fonte — app finance-control)

- `backend/src/main/java/br/com/financecontrol/controller/AuthController.java`
- `backend/src/main/java/br/com/financecontrol/service/AuthService.java`
- `backend/src/main/java/br/com/financecontrol/dto/auth/RegisterRequest.java` / `LoginRequest.java` / `AuthResponse.java` / `UserSummary.java`
- `backend/src/main/java/br/com/financecontrol/security/JwtService.java` / `SecurityConfig.java`
- `backend/src/main/java/br/com/financecontrol/exception/GlobalExceptionHandler.java` / `ApiError.java`
- `backend/src/main/resources/messages.properties`
- `frontend/src/pages/LoginPage.tsx`
- `frontend/src/hooks/useAuth.tsx`
- `frontend/src/services/authService.ts` / `api.ts`
- `frontend/src/routes/AppRoutes.tsx`