# Demo

Demonstração pública e **somente leitura** da API, com dados fictícios:

- Swagger UI: `https://livro-da-guilda.onrender.com/swagger-ui.html`
- Health: `https://livro-da-guilda.onrender.com/actuator/health`

A raiz (`https://livro-da-guilda.onrender.com/`) responde `302` para o Swagger UI.
Fora do perfil demo a raiz continua protegida e devolve `401`.

O plano free do Render hiberna após alguns minutos sem acesso; o primeiro request
pode levar 30–60 s. Se a URL acima não responder, veja [deploy-render.md](deploy-render.md).

## Credenciais

| Campo | Valor |
|---|---|
| E-mail | `demo@eliascampos.dev` |
| Senha | `demo-readonly-2026` |

São públicas de propósito: a demo recusa qualquer escrita. Para trocar, defina
`DEMO_ADMIN_EMAIL`/`DEMO_ADMIN_PASSWORD` no Render e recrie o banco.

## O que funciona

| Ação | Resultado |
|---|---|
| `POST /auth/login` | `200` com `accessToken` e `refreshToken` |
| `POST /auth/refresh` | `200` com par novo (rotação) |
| `POST /auth/logout` | `204` |
| `GET /api/v1/users` (com filtros e paginação) | `200` |
| `GET /api/v1/users/{id}` | `200` |
| `POST`, `PUT`, `PATCH`, `DELETE` em `/api/v1/**` | `403` `READ_ONLY_DEMO` |
| Swagger UI e `/v3/api-docs` | `200` |

A recusa de escrita é do servidor (`DemoReadOnlyFilter`), não do Swagger: quem chamar
direto com `curl` também recebe `403`.

```json
{
  "type": "about:blank",
  "title": "Demo somente leitura",
  "status": 403,
  "detail": "Esta demo e somente leitura: consulte os dados com GET, mas nao altere nada.",
  "instance": "/api/v1/users",
  "code": "READ_ONLY_DEMO",
  "timestamp": "2026-11-18T20:00:00Z"
}
```

## Por que somente leitura

Um admin público com escrita permitiria que qualquer visitante apagasse os dados ou
usasse a API como hospedagem gratuita. O GIF e a coleção do Postman mostram o fluxo
completo de escrita rodando localmente; a demo pública mostra contrato, autenticação,
paginação, filtros e erros — que é o que um recrutador precisa ver.

## Rodar a demo localmente

Com um MongoDB real (a demo não usa o Mongo embutido do perfil `dev`):

```powershell
$env:SPRING_PROFILES_ACTIVE="demo"
$env:MONGODB_URI="mongodb://localhost:27018/usuarios-demo"
$env:JWT_SECRET="demo-local-secret-change-me-0123456789"
$env:DEMO_ADMIN_PASSWORD="demo-readonly-2026"
mvn spring-boot:run
```

Alternativa: `docker compose up --build` e mude `SPRING_PROFILES_ACTIVE` para `demo`
no `docker-compose.yml`.

## Postman

1. Importe [`postman/livro-da-guilda.postman_collection.json`](../postman/livro-da-guilda.postman_collection.json)
   e [`postman/demo.postman_environment.json`](../postman/demo.postman_environment.json).
2. Selecione o environment **livro-da-guilda demo** e ajuste `baseUrl` se o Render tiver
   adicionado sufixo ao nome.
3. Rode **Auth → Login**: o script salva `accessToken` e `refreshToken` nas variáveis.
4. As requisições de leitura funcionam na demo; as de escrita retornam `403` de propósito.
   Para exercitar escrita, rode a API local em `dev` e troque `baseUrl` para
   `http://localhost:8080`.
