# Livro da Guilda

[![CI](https://github.com/EliasCamposDEV/livro-da-guilda/actions/workflows/ci.yml/badge.svg)](https://github.com/EliasCamposDEV/livro-da-guilda/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/github/license/EliasCamposDEV/livro-da-guilda)](LICENSE)

**[Demo pública](https://livro-da-guilda.onrender.com/swagger-ui.html)** (somente leitura) · **[Swagger local](http://localhost:8080/swagger-ui.html)** — suba com `mvn spring-boot:run` e explore os endpoints na documentação interativa.

Na demo, faça login em `POST /auth/login` com `demo@eliascampos.dev` / `demo-readonly-2026`, copie o `accessToken`, clique em **Authorize** e cole só o token (sem "Bearer"). Clique em um endpoint para expandir e depois em **Execute**. A demo é somente leitura: `GET` funciona, `POST`/`PUT`/`PATCH`/`DELETE` respondem `403`. O primeiro acesso pode levar ~1 minuto (instância free dorme).

API REST para CRUD de usuários, com validação, paginação, filtros e documentação OpenAPI. Java 21, Spring Boot 3.5, MongoDB.

O nome vem de RPG: o livro da guilda registra quem é membro, qual papel tem e o que pode fazer — a mesma ideia de um cadastro de usuários com JWT e papéis (`ADMIN`).

## Demo

| | |
|---|---|
| Swagger UI | `https://livro-da-guilda.onrender.com/swagger-ui.html` |
| Login | `demo@eliascampos.dev` / `demo-readonly-2026` |
| Dados | 7 usuários fictícios criados no primeiro start |
| Escrita | bloqueada com `403 READ_ONLY_DEMO` |
| Postman | [coleção](postman/livro-da-guilda.postman_collection.json) · [environment](postman/demo.postman_environment.json) |

A demo é **somente leitura de propósito**: qualquer pessoa pode logar e ler, mas
`POST`/`PUT`/`PATCH`/`DELETE` em `/api/v1/**` retornam `403`. A raiz do site
(`https://livro-da-guilda.onrender.com/`) redireciona para o Swagger, então dá
para mandar esse link para alguém sem explicar nada. Para ver o fluxo de escrita
completo, rode localmente ([docs/demo.md](docs/demo.md)); a coleção do Postman funciona nos dois modos. O plano free
do Render hiberna por inatividade — o primeiro request pode levar 30–60 s.

Como rodar, limpar e o que está bloqueado: [docs/demo.md](docs/demo.md). Para publicar
sua própria instância: [docs/deploy-render.md](docs/deploy-render.md).

## Rodar

```bash
mvn spring-boot:run
```

Perfil `dev` sobe um MongoDB embarcado (primeira execução baixa o binário). API em http://localhost:8080.

Com MongoDB real (porta 27018):

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:MONGODB_URI="mongodb://localhost:27018/usuarios"
mvn spring-boot:run
```

`MONGODB_URI` é obrigatória em `prod`; sem ela a aplicação não sobe.

Precisa de JDK 21 (o `mvn` falha se o `JAVA_HOME` apontar para outra versão).

O projeto **não lê `.env`**: o Spring Boot só enxerga variáveis do ambiente/shell. O `.env.example` serve de referência, e o Docker Compose usa um `.env` na raiz para substituir `${...}` dentro do `compose`.

## Docker

```bash
docker compose up --build
```

API em http://127.0.0.1:8081, MongoDB apenas na rede interna do Compose. Para inspecionar o banco: `docker compose exec mongo mongosh usuarios`.

Na primeira subida, com o banco vazio, o Compose cria o admin `admin@local.dev` / `admin12345` (dá para trocar com `BOOTSTRAP_ADMIN_EMAIL` e `BOOTSTRAP_ADMIN_PASSWORD` no shell ou no `.env`). Sem isso não haveria usuário para logar.

> Os exemplos de `curl` abaixo usam a porta `8080` (perfil `dev`). Com Docker, troque por `8081`.

## Autenticação

`/api/v1/users/**` exige o papel `ADMIN`. Login, Swagger e health check são públicos.

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@email.com","password":"admin12345"}'
```

A resposta traz `accessToken` (1 hora) e `refreshToken` (7 dias); use o access nas demais chamadas:

```bash
curl http://localhost:8080/api/v1/users -H "Authorization: Bearer <accessToken>"
```

Quando o access token expirar, troque o refresh por um par novo:

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'
```

O refresh é **rotativo**: cada uso devolve um par novo e invalida o refresh anterior. Só o refresh mais recente de cada usuário vale — um novo login (ou refresh) derruba os anteriores. O access token já emitido continua válido até expirar (1 hora).

O refresh devolve 401 com `code: INVALID_REFRESH_TOKEN` se estiver expirado, adulterado, se for um access token, se já tiver sido usado, se o usuário foi desativado ou se outro login invalidou aquela sessão. Um refresh token não é aceito como access token nas rotas protegidas (401).

Para encerrar a sessão, use o access token em `/auth/logout` (204); o refresh emitido deixa de valer:

```bash
curl -X POST http://localhost:8080/auth/logout -H "Authorization: Bearer <accessToken>"
```

Trocar a própria senha exige a senha atual e também derruba os refresh tokens anteriores:

```bash
curl -X PUT http://localhost:8080/api/v1/me/password \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword":"admin12345","newPassword":"novaSenha123"}'
```

Senha atual errada devolve 400 com `code: INVALID_PASSWORD`.

- O login usa os usuários do próprio banco (senha em BCrypt). Usuário com `active: false` não autentica.
- O papel vem do campo `role`: token de `USER` recebe 403 em `/api/v1/users/**`.
- Em `dev` um admin é criado na primeira subida: `admin@email.com` / `admin12345`.
- Em `prod`, defina `APP_BOOTSTRAP_ADMIN_EMAIL` e `APP_BOOTSTRAP_ADMIN_PASSWORD` para criar o primeiro `ADMIN` em banco vazio; sem as variáveis nada é criado (no `docker compose` elas já vêm definidas).
- O perfil `demo` usa a mesma postura de `prod`, cria usuários fictícios e recusa escrita em `/api/v1/**` com `403 READ_ONLY_DEMO`; veja [docs/demo.md](docs/demo.md).
- `JWT_SECRET` (mínimo 32 caracteres, HS256) é obrigatória em `prod`; sem ela a aplicação não sobe.
- Validades em `app.security.jwt.ttl-seconds` (access) e `refresh-ttl-seconds` (refresh).
- `/auth/login` bloqueia após 5 tentativas erradas por e-mail + IP em 15 minutos (429 `TOO_MANY_ATTEMPTS`), em memória e por instância.
- CORS é desligado por padrão; libere origens com `CORS_ALLOWED_ORIGINS` (separadas por vírgula).
- Deploy, backup e rotação de segredo: [docs/operacao.md](docs/operacao.md).

## Endpoints

Base: `/api/v1/users`

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/users` | Cria usuário |
| GET | `/api/v1/users` | Lista com paginação e filtros |
| GET | `/api/v1/users/{id}` | Busca por ID |
| PUT | `/api/v1/users/{id}` | Atualiza |
| PATCH | `/api/v1/users/{id}/status` | Ativa/desativa |
| DELETE | `/api/v1/users/{id}` | Remove |
| PUT | `/api/v1/me/password` | Troca a senha do usuário autenticado |
| POST | `/auth/logout` | Invalida os refresh tokens do usuário autenticado |

Parâmetros de listagem: `page`, `size` (máx. 50), `sort`, `search`, `role` (USER/ADMIN), `active` (true/false).

Exemplo de criação:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Maria Silva","email":"maria.silva@email.com","role":"USER","password":"senha12345"}'
```

Erros retornam `application/problem+json` com `code`: `VALIDATION_ERROR`, `INVALID_REQUEST`, `RESOURCE_NOT_FOUND`, `EMAIL_ALREADY_EXISTS`, `INVALID_CREDENTIALS`, `INVALID_REFRESH_TOKEN`, `INVALID_PASSWORD`, `TOO_MANY_ATTEMPTS`, `INTERNAL_ERROR`.

## Testes

```bash
mvn test
```

32 testes (25 de integração com MongoDB embarcado e 7 unitários) cobrindo criação, leitura, atualização, ativação, exclusão, paginação, busca, filtros, e-mail duplicado, validação, recurso inexistente, os erros 404/405 e autenticação (401 sem token ou credencial inválida, 403 para quem não é ADMIN, emissão, rotação e troca de refresh token, recusa de token com o tipo errado ou já rotacionado, troca de senha, logout e bloqueio por excesso de tentativas). Os testes do modo demo garantem que login e leitura continuam funcionando e que toda escrita em `/api/v1/**` recebe `403 READ_ONLY_DEMO` sem alterar o banco.

O `mvn verify` também roda o SpotBugs (falha em achados de prioridade alta).

## Notas

- E-mail é normalizado para minúsculas e protegido por índice único.
- O índice único de e-mail é criado na subida (`auto-index-creation`). Para várias instâncias ou deploy sem downtime, mova a criação de índices para um passo de migração.